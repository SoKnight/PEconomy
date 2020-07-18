package ru.soknight.peconomy.database;

import java.sql.Date;
import java.text.DateFormat;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@DatabaseTable(tableName = "transactions")
public class Transaction {

	@DatabaseField(generatedId = true)
	private int id;
	@DatabaseField
	private String owner;
	@DatabaseField
	private String currency;
	@Setter
	@DatabaseField
	private TransactionType type;
	@DatabaseField
	private float preBalance;
	@DatabaseField
	private float postBalance;
	@DatabaseField
	private String source;
	@DatabaseField
	private Date date;
	
	public Transaction(String owner, String currency, TransactionType type, float pre, float post, String source) {
		this.owner = owner;
		this.currency = currency;
		this.type = type;
		this.preBalance = pre;
		this.postBalance = post;
		this.source = source;
		this.date = new Date(System.currentTimeMillis());
	}
	
	public String formatDate(DateFormat format) {
		try {
			return format.format(this.date);
		} catch (Exception e) {
			System.err.println("Failed to format transaction date with pattern '" + format + "': " + e.getMessage());
			return "???";
		}
	}
	
	public String formatFloat(float source) {
		return String.format("%.2f", source);
	}
	
	public boolean isSuccessed() {
		return this.type != TransactionType.FAILED;
	}
	
}
