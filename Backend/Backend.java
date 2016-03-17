package Backend;

import java.util.*;
import java.lang.*;
import java.io.*;

public class Backend {

	//Need to grab accounts and transactions from the File IO class.
	static ArrayList<Account> accounts = new ArrayList<Account>();
	static ArrayList<Transaction> transactions = new ArrayList<Transaction>();

	//Login data
	static private boolean admin;
	static private String loginname;
	static private boolean loggedin;

	static private Validator validator;
	static private Account tempAcc;
	static private float sessionWithdrawn = 0;

	static private void applyWithdrawal(Transaction transaction){
		if (validator.sessionLoggedIn(loggedin)) {
			tempAcc = validator.findAccount(transaction.getNum(), accounts);
			//check login credentials
			if (admin || (validator.accountMatchName(loginname, tempAcc))) {
				if (tempAcc.getBalance() >= transaction.getAmount()) {
					validator.findAccount(transaction.getNum(), accounts).setBalance(tempAcc.getBalance() - transaction.getAmount());
					sessionWithdrawn += transaction.getAmount();
					validator.findAccount(transaction.getNum(), accounts).incTransactions();
				}
			}			
		}
	}

	static private void applyTransfer(Transaction sourcetransaction, Transaction desttransaction){
		if (validator.sessionLoggedIn(loggedin)) {
			tempAcc = validator.findAccount(sourcetransaction.getNum(), accounts);
			tempDestAcc = validator.findAccount(sourcetransaction.getNum(), accounts);
			//check login credentials
			if (admin || (validator.accountMatchName(loginname, tempAcc))) {
				//set balances of the two accounts
				validator.findAccount(sourcetransaction.getNum(), accounts).setBalance(tempAcc.getBalance() - sourcetransaction.getAmount());
				validator.findAccount(desttransaction.getNum(), accounts).setBalance(tempDestAcc.getBalance() + sourcetransaction.getAmount());
				//increment the two transaction counts
				validator.findAccount(sourcetransaction.getNum(), accounts).incTransactions();
				validator.findAccount(desttransaction.getNum(), accounts).incTransactions();
			}			
		}
	}

	static private void applyPayBill(Transaction transaction){
		if (validator.sessionLoggedIn(loggedin)) {
			tempAcc = validator.findAccount(transaction.getNum(), accounts);
			//check login credentials
			if (admin || (validator.accountMatchName(loginname, tempAcc))) {
				//check if the comapny exists
				if(validator.checkCompany(transaction.getCode())){
					if (tempAcc.getBalance() >= transaction.getAmount()) {
						validator.findAccount(transaction.getNum(), accounts).setBalance(tempAcc.getBalance() - transaction.getAmount());					
						validator.findAccount(transaction.getNum(), accounts).incTransactions();
					}				
				}
			}			
		}
	}

	static private void applyDeposit(Transaction transaction){
		if (validator.sessionLoggedIn(loggedin)) {
			tempAcc = validator.findAccount(transaction.getNum(), accounts);
			//check login credentials
			if (admin || (validator.accountMatchName(loginname, tempAcc))) {
					validator.findAccount(transaction.getNum(), accounts).setBalance(tempAcc.getBalance() + transaction.getAmount());
					validator.findAccount(transaction.getNum(), accounts).incTransactions();
			}			
		}
	}

	static private void applyCreate(Transaction transaction){
		if (validator.sessionLoggedIn(loggedin)) {
			//check login credentials
			if (admin) {
				tempAcc.setNum(validator.findNextNumber(accounts));
				tempAcc.setName(transaction.getName());
				tempAcc.setStatus("A");
				tempAcc.setBalance(transaction.getAmount());
				tempAcc.setTransactions(0);
				tempAcc.setStudent(false);
				tempAcc.setCreated(true);
			}else {
				System.out.println("ERROR: Attempting to create without admin credentials");
			}
		}
	}

	static private void applyDelete(Transaction transaction){
		if (validator.sessionLoggedIn(loggedin)) {
			tempAcc = validator.findAccount(transaction.getNum(), accounts);
			//check if we've found the account
			if (!(tempAcc == null)) {
				//check login credentials
				if (admin) {
					//check if the account is a student account
					if (validator.accountMatchName(loginname, tempAcc)) {
						accounts.remove(tempAcc);
					}else {
						System.out.println("ERROR: Could not find account");
					}
				}else {
					System.out.println("ERROR: Attempting to delete without admin credentials");
				}
			}
		}
	}

	static private void applyDisable(Transaction transaction){
		if (validator.sessionLoggedIn(loggedin)) {
			tempAcc = validator.findAccount(transaction.getNum(), accounts);
			//check if we've found the account
			if (!(tempAcc == null)) {
				//check login credentials
				if (admin || (validator.accountMatchName(loginname, tempAcc))) {
					//check if the account is a student account
					if (!(tempAcc.getStatus().equals("A"))) {
						validator.findAccount(transaction.getNum(), accounts).setStudent(true);
						validator.findAccount(transaction.getNum(), accounts).incTransactions();
					}else {
						System.out.println("ERROR: Attempting to disable a disabled account");
					}
				}
			}
		}
	}

	static private void applyEnable(Transaction transaction){
		if (validator.sessionLoggedIn(loggedin)) {
			tempAcc = validator.findAccount(transaction.getNum(), accounts);
			//check if we've found the account
			if (!(tempAcc == null)) {
				//check login credentials
				if (admin || (validator.accountMatchName(loginname, tempAcc))) {
					//check if the account is a student account
					if (!(tempAcc.getStatus().equals("D"))) {
						validator.findAccount(transaction.getNum(), accounts).setStudent(true);
						validator.findAccount(transaction.getNum(), accounts).incTransactions();
					}else {
						System.out.println("ERROR: Attempting to enable an enabled account");
					}
				}
			}
		}
	}

	static private void applyChangePlan(Transaction transaction){
		if (validator.sessionLoggedIn(loggedin)) {
			tempAcc = validator.findAccount(transaction.getNum(), accounts);
			//check if we've found the account
			if (!(tempAcc == null)) {
				//check login credentials
				if (admin || (validator.accountMatchName(loginname, tempAcc))) {
					//check if the account is a student account
					if (!(tempAcc.getStudent() == true)) {
						validator.findAccount(transaction.getNum(), accounts).setStudent(true);
						validator.findAccount(transaction.getNum(), accounts).incTransactions();
					}else {
						System.out.println("ERROR: Attempting to change plan on a student account");
					}
				}
			}
		}
	}

	static private void applyEndofSession(Transaction transaction){
		// Iterate through the accounts list
		for (int i = 0; i < accounts.size();i++) {
			//unfreeze all the balances
			accounts.get(i).setFrozen(0);
			//change all the accounts to "not made today"
			accounts.get(i).setCreated(false);
		}
		sessionWithdrawn = 0;
	}

	static private void applyLogin(Transaction transaction){
		//Check if logged in already
		if (!(loggedin)){

			//check the misc field for admin login
			if(transaction.getMisc().equals("S")){
				admin = false;
			}else {
				admin = true;
			}

			//set the login name & param
			loginname = transaction.getName();
			loggedin = true;
		}else{
			System.out.println("ERROR: Log in transaction when logged in already");
		}
	}

	public static void main(String[] args) {
		admin = false;
		loginname = "";
		loggedin = false;
		//get accounts and transactions arraylists from writer

		//iterate over all the entires in the transactions arraylist
		for (int i = 0; i < transactions.size();i++) {
			switch (transactions.get(i).getCode()){
				case "01":
					applyWithdrawal(transactions.get(i));
					break;
				case "02":
					//transfer
					//need to not be at the last transaction
					if (i + 1 != transactions.size()) {
						//need to check if next transaction is also transfer
						if (transactions.get(i+1).getCode().equals("02")){
							//apply transfer to this transaction and next transaction
							applyTransfer(transactions.get(i), transactions.get(i+1));
							//skip to next transaction
							i++;
						}else{
							System.out.println("Transfer transactions must come in pairs");
						}
					}else{
						System.out.println("Transfer transaction cannot be last");
					}
					break;
				case "03":
					applyPayBill(transactions.get(i));
					break;
				case "04":
					applyDeposit(transactions.get(i));
					break;
				case "05":
					applyCreate(transactions.get(i));
					break;
				case "06":
					applyDelete(transactions.get(i));
					break;
				case "07":
					applyDisable(transactions.get(i));
					break;
				case "08":
					applyChangePlan(transactions.get(i));
					break;
				case "09":
					applyEnable(transactions.get(i));
					break;
				case "10":
					applyLogin(transactions.get(i));
					break;
				case "00":
					applyEndofSession(transactions.get(i));
					break;
			}
		}

		//pass back the accounts list
	}
}