//
//  ViewController.swift
//  SwiftStarter
//
//  Created by Muhammad Fauzi Masykur on 08/07/20.
//  Copyright © 2020 Muhammad Fauzi Masykur. All rights reserved.
//

import UIKit
import GopayCheckoutKit

class PaymentViewController: UIViewController {
    
    var merchantServerUrl = ""
    var phoneNumber = ""
    var linkAccountResult = GPYLinkAccountResult()
    var accountInfo = GPYAccountInfo()
    
    @IBOutlet weak var accountIdLabel: UILabel!
    @IBOutlet weak var accountStatusLabel: UILabel!
    @IBOutlet weak var walletNameLabel: UILabel!
    @IBOutlet weak var walletStatusLabel: UILabel!
    @IBOutlet weak var walletTokenLabel: UILabel!
    @IBOutlet weak var walletBalanceLabel: UILabel!
    
    override func viewDidLoad() {
        super.viewDidLoad()
    }
    @IBAction func linAccount(_ sender: Any) {
        
        let partnerDetails = GPYPartnerDetails(phoneNumber: self.phoneNumber, countryCode: "62")
        
        GPYClient.linkAccount(withPaymentType: "gopay", gopayPartnerDetails: partnerDetails, viewController: self) { (result, error) in
            if (result != nil){
                self.linkAccountResult = result!
                self.accountIdLabel.text = "Account id : \(self.linkAccountResult.accountID ?? "")"
                self.accountStatusLabel.text = "Account status : \(self.linkAccountResult.accountStatus ?? "")"
            } else {
                self.showErrorAlert(message: "Link account error", error: error)
            }
            
        }
    }
    
    @IBAction func enquireAccount(_ sender: Any) {
        
        if (self.linkAccountResult.accountID != nil) {
            GPYClient.enquireAccount(withAccountID: self.linkAccountResult.accountID!) { (accountInfo, error) in
                
                if (accountInfo != nil) {
                    self.accountInfo = accountInfo!
                    self.walletNameLabel.text = "Name : \(self.accountInfo.metadata?.paymentOptions?.first?.name ?? "")"
                    self.walletStatusLabel.text = "Status : \( (self.accountInfo.metadata?.paymentOptions?.first?.active) ?? false)"
                    self.walletTokenLabel.text = "Token : \(self.accountInfo.metadata?.paymentOptions?.first?.token ?? "")"
                    self.walletBalanceLabel.text = "Balance: \(self.accountInfo.metadata?.paymentOptions?.first?.balance?.value ?? "")"
                } else {
                    self.showErrorAlert(message: "Enquire account error", error: error)
                }
                
            }
        } else {
            self.showErrorAlert(message: "Account id is null, link the account first", error: nil)
        }
        
    }
    
    @IBAction func disableAccount(_ sender: Any) {
        
        if (self.linkAccountResult.accountID != nil) {
            
            GPYClient.disableAccount(withAccountID: self.linkAccountResult.accountID ?? "") { (disableAccountResult, error) in
                
                if (disableAccountResult != nil){
                    self.accountStatusLabel.text = disableAccountResult?.accountStatus
                    self.walletNameLabel.text = ""
                    self.walletStatusLabel.text = ""
                    self.walletTokenLabel.text = ""
                    self.walletBalanceLabel.text = ""
                } else {
                    self.showErrorAlert(message: "Disable account error", error: error)
                }
                
            }
        } else {
            self.showErrorAlert(message: "Account id is null, link the account first", error: nil)
        }
    }
    
    @IBAction func createTransaction(_ sender: Any) {
        
        if ((self.accountInfo.accountID != nil) && self.accountInfo.metadata?.paymentOptions != nil) {
            
            let gopayDetais = GPYGopayDetails(accountID: self.accountInfo.accountID ?? "", paymentOptionToken: self.accountInfo.metadata?.paymentOptions?.first?.token ?? "")
            
            let orderId = generateRandomOrderId()
            
            let transDetails = GPYTransactionDetails(grossAmount: 200, orderID: orderId, currency: "IDR")
            let cusDetails = GPYCustomerDetails(phoneNumber: "088888888", firstName: "fauzi", lastName: "cihuy", email: "emailemail@gmail.com")
            let itemDetails1 = GPYItemDetails(ithItemID: "aabb1", name: "one piece t-shirt", price: 100, quantity: 1, category: "shirts")
            let itemDetails2 = GPYItemDetails(ithItemID: "aabb2", name: "one piece short", price: 100, quantity: 1, category: "pants")
            
            GPYClient.createTransaction(withPaymentType: "gopay", viewController: self, gopayDetails: gopayDetais, transactionDetails: transDetails, customerDetails: cusDetails, itemDetails: [itemDetails1, itemDetails2]) { (result, error) in
                
                if (result != nil) {
                    let alert = UIAlertController(title: "Success", message: "Transacation with ID : \(result!.transactionId) is \(result!.transactionStatus)", preferredStyle: .alert)
                    let action = UIAlertAction(title: "OK", style: .default, handler: nil)
                    alert.addAction(action)
                    self.present(alert, animated: true, completion: nil)
                } else {
                    self.showErrorAlert(message: "Create transaction error", error: error)
                }
                
            }
        } else {
            self.showErrorAlert(message: "Account id / Payment option token is null, enquire the account first", error: nil)
        }
        
    }
    
    func generateRandomOrderId() -> String {
        let date = Date()
        let df = DateFormatter()
        df.dateFormat = "yyyy-MM-dd HHmmssSSS"
        let dateString = df.string(from: date)
        return dateString
    }
    func showErrorAlert(message : String, error : Error?) {
        let alert = UIAlertController(title: "Error", message: "\(message) error : \(error?.localizedDescription ?? "error" )", preferredStyle: .alert)
        let closeAction = UIAlertAction(title: "Close", style: .cancel, handler: nil)
        alert.addAction(closeAction)
        self.present(alert, animated: true, completion: nil)
    }
    
}

