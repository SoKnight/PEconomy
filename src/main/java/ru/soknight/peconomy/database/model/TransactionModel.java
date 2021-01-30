package ru.soknight.peconomy.database.model;

import java.util.Date;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@DatabaseTable(tableName = "transactions")
public class TransactionModel {

    @DatabaseField(generatedId = true)
    private int id;
    @DatabaseField(columnName = "owner")
    private String walletHolder;
    @DatabaseField(columnName = "source")
    private String operator;
    @DatabaseField
    private String currency;
    @DatabaseField
    private String type;
    @DatabaseField
    private float preBalance;
    @DatabaseField
    private float postBalance;
    @DatabaseField(columnName = "date", dataType = DataType.DATE)
    private Date timestamp;
    
    public TransactionModel(
            String walletHolder, String operator, String currency,
            String type, float pre, float post
    ) {
        this.walletHolder = walletHolder;
        this.operator = operator;
        this.currency = currency;
        
        this.type = type;
        this.preBalance = pre;
        this.postBalance = post;
        
        this.timestamp = new Date(System.currentTimeMillis());
    }
    
    public boolean isSuccess() {
        return !type.equalsIgnoreCase("failed");
    }
    
}
