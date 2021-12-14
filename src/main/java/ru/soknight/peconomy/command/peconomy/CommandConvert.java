package ru.soknight.peconomy.command.peconomy;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.soknight.lib.argument.CommandArguments;
import ru.soknight.lib.command.preset.subcommand.ArgumentableSubcommand;
import ru.soknight.lib.configuration.Messages;
import ru.soknight.peconomy.api.PEconomyAPI;
import ru.soknight.peconomy.configuration.CurrenciesManager;
import ru.soknight.peconomy.configuration.CurrencyInstance;
import ru.soknight.peconomy.database.DatabaseManager;
import ru.soknight.peconomy.database.model.TransactionModel;
import ru.soknight.peconomy.database.model.WalletModel;
import ru.soknight.peconomy.format.Formatter;
import ru.soknight.peconomy.transaction.TransactionCause;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class CommandConvert extends ArgumentableSubcommand {

    private final Messages messages;
    private final DatabaseManager databaseManager;
    private final CurrenciesManager currenciesManager;

    public CommandConvert(Messages messages, DatabaseManager databaseManager, CurrenciesManager currenciesManager) {
        super("peco.command.convert", 3, messages);

        this.messages = messages;
        this.databaseManager = databaseManager;
        this.currenciesManager = currenciesManager;
    }

    @Override
    protected void executeCommand(CommandSender sender, CommandArguments args) {
        float amount = args.getAsFloat(0);
        if(amount <= 0F) {
            messages.sendFormatted(sender, "error.arg-is-not-float", "%arg%", args.get(0));
            return;
        }

        CurrencyInstance firstCurrency = currenciesManager.getCurrency(args.get(1));
        if(firstCurrency == null) {
            messages.sendFormatted(sender, "error.unknown-currency", "%currency%", args.get(1));
            return;
        }

        CurrencyInstance secondCurrency = currenciesManager.getCurrency(args.get(2));
        if(secondCurrency == null) {
            messages.sendFormatted(sender, "error.unknown-currency", "%currency%", args.get(2));
            return;
        }

        if(firstCurrency.getId().equals(secondCurrency.getId())) {
            messages.getAndSend(sender, "convert.failed.same-named-currency");
            return;
        }

        if(!firstCurrency.isConvertableTo(secondCurrency)) {
            messages.sendFormatted(sender, "convert.failed.unconvertable",
                    "%currency_first_name%", firstCurrency.getName(),
                    "%currency_first%", firstCurrency.getSymbol(),
                    "%currency_second_name%", secondCurrency.getName(),
                    "%currency_second%", secondCurrency.getSymbol()
            );
            return;
        }

        String walletHolder0 = null;
        if(args.size() > 3 && sender.hasPermission("peco.command.convert.other"))
            walletHolder0 = args.get(3);

        if(walletHolder0 == null) {
            if(!isPlayer(sender)) {
                messages.getAndSend(sender, "error.wrong-syntax");
                return;
            }

            walletHolder0 = sender.getName();
        }

        String walletHolder = walletHolder0;
        boolean other = !isPlayer(sender) || !walletHolder.equals(sender.getName());

        CompletableFuture<WalletModel> walletFuture = other
                ? databaseManager.getWallet(walletHolder)
                : databaseManager.getOrCreateWallet((Player) sender);

        Formatter formatter = PEconomyAPI.get().getFormatter();
        walletFuture.thenAccept(wallet -> {
            if(wallet == null) {
                messages.sendFormatted(sender, "convert.failed.empty-wallet", "%player%", walletHolder);
                return;
            }

            float firstCurrencyBalancePre = wallet.getAmount(firstCurrency.getId());
            float firstCurrencyBalancePost = firstCurrencyBalancePre - amount;

            if(firstCurrencyBalancePost < 0F) {
                if(other)
                    messages.sendFormatted(sender, "convert.failed.not-enough.other",
                            "%player%", walletHolder,
                            "%amount%", formatter.formatAmount(firstCurrencyBalancePre),
                            "%requested%", formatter.formatAmount(amount),
                            "%currency%", firstCurrency.getSymbol()
                    );
                else
                    messages.sendFormatted(sender, "convert.failed.not-enough.self",
                            "%amount%", formatter.formatAmount(firstCurrencyBalancePre),
                            "%requested%", formatter.formatAmount(amount),
                            "%currency%", firstCurrency.getSymbol()
                    );
                return;
            }

            float converted = firstCurrency.convert(secondCurrency, amount);

            float secondCurrencyBalancePre = wallet.getAmount(secondCurrency.getId());
            float secondCurrencyBalancePost = secondCurrencyBalancePre + converted;

            float limit = secondCurrency.getLimit();
            if(limit > 0F && secondCurrencyBalancePost > limit) {
                messages.sendFormatted(sender, "convert.failed.limit-reached",
                        "%limit%", formatter.formatAmount(limit),
                        "%currency%", secondCurrency.getSymbol()
                );
                return;
            }

            wallet.takeAmount(firstCurrency.getId(), amount);
            wallet.addAmount(secondCurrency.getId(), converted);

            databaseManager.saveWallet(wallet).join();

            String operator = isPlayer(sender) ? sender.getName() : null;

            TransactionModel firstTransaction = new TransactionModel(
                    walletHolder,
                    firstCurrency.getId(),
                    firstCurrencyBalancePre,
                    firstCurrencyBalancePost,
                    operator,
                    TransactionCause.CONVERTATION
            );

            TransactionModel secondTransaction = new TransactionModel(
                    walletHolder,
                    secondCurrency.getId(),
                    secondCurrencyBalancePre,
                    secondCurrencyBalancePost,
                    operator,
                    TransactionCause.CONVERTATION
            );

            databaseManager.saveTransaction(firstTransaction).join();
            databaseManager.saveTransaction(secondTransaction).join();

            if(!other) {
                messages.sendFormatted(sender, "convert.success.operator.self",
                        "%amount_first%", formatter.formatAmount(amount),
                        "%currency_first%", firstCurrency.getSymbol(),
                        "%amount_second%", formatter.formatAmount(converted),
                        "%currency_second%", secondCurrency.getSymbol(),
                        "%currency_first_name%", firstCurrency.getName(),
                        "%amount_first_from%", formatter.formatAmount(firstCurrencyBalancePre),
                        "%amount_first_to%", formatter.formatAmount(firstCurrencyBalancePost),
                        "%id_first%", firstTransaction.getId(),
                        "%currency_second_name%", secondCurrency.getName(),
                        "%amount_second_from%", formatter.formatAmount(secondCurrencyBalancePre),
                        "%amount_second_to%", formatter.formatAmount(secondCurrencyBalancePost),
                        "%id_second%", secondTransaction.getId()
                );
                return;
            }

            messages.sendFormatted(sender, "convert.success.operator.other",
                    "%player%", walletHolder,
                    "%amount_first%", formatter.formatAmount(amount),
                    "%currency_first%", firstCurrency.getSymbol(),
                    "%amount_second%", formatter.formatAmount(converted),
                    "%currency_second%", secondCurrency.getSymbol(),
                    "%currency_first_name%", firstCurrency.getName(),
                    "%amount_first_from%", formatter.formatAmount(firstCurrencyBalancePre),
                    "%amount_first_to%", formatter.formatAmount(firstCurrencyBalancePost),
                    "%id_first%", firstTransaction.getId(),
                    "%currency_second_name%", secondCurrency.getName(),
                    "%amount_second_from%", formatter.formatAmount(secondCurrencyBalancePre),
                    "%amount_second_to%", formatter.formatAmount(secondCurrencyBalancePost),
                    "%id_second%", secondTransaction.getId()
            );

            Player onlineHolder = Bukkit.getPlayer(walletHolder);
            if(onlineHolder != null && onlineHolder.isOnline())
                messages.sendFormatted(onlineHolder, "convert.success.holder",
                        "%amount_first%", formatter.formatAmount(amount),
                        "%currency_first%", firstCurrency.getSymbol(),
                        "%amount_second%", formatter.formatAmount(converted),
                        "%currency_second%", secondCurrency.getSymbol(),
                        "%currency_first_name%", firstCurrency.getName(),
                        "%amount_first_from%", formatter.formatAmount(firstCurrencyBalancePre),
                        "%amount_first_to%", formatter.formatAmount(firstCurrencyBalancePost),
                        "%id_first%", firstTransaction.getId(),
                        "%currency_second_name%", secondCurrency.getName(),
                        "%amount_second_from%", formatter.formatAmount(secondCurrencyBalancePre),
                        "%amount_second_to%", formatter.formatAmount(secondCurrencyBalancePost),
                        "%id_second%", secondTransaction.getId()
                );
        });
    }

    @Override
    protected List<String> executeTabCompletion(CommandSender sender, CommandArguments args) {
        if(args.size() < 2 || args.size() > 4)
            return null;

        if(args.size() > 3 && !sender.hasPermission("peco.command.convert.other"))
            return null;

        String arg = getLastArgument(args, true);
        if(args.size() == 2) {
            return currenciesManager.getCurrenciesIDs().stream()
                    .filter(id -> id.toLowerCase().startsWith(arg))
                    .collect(Collectors.toList());
        }

        CurrencyInstance firstCurrency = currenciesManager.getCurrency(args.get(1));
        if(firstCurrency == null)
            return null;

        if(args.size() == 3) {
            return firstCurrency.getConvertationSetup().getCurrenciesIDs().stream()
                    .filter(id -> id.toLowerCase().startsWith(arg))
                    .collect(Collectors.toList());
        }

        if(!currenciesManager.isCurrency(args.get(2)))
            return null;

        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(n -> n.toLowerCase().startsWith(arg))
                .collect(Collectors.toList());
    }

}
