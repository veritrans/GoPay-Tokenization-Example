//
//  ConfigViewController.swift
//  SwiftStarter
//
//  Created by Muhammad Fauzi Masykur on 08/07/20.
//  Copyright © 2020 Muhammad Fauzi Masykur. All rights reserved.
//

import UIKit

class ConfigViewController: UIViewController {
    
    @IBOutlet weak var phoneNumberTF: UITextField!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        self.phoneNumberTF.text = "82221009967"
    }
    @IBAction func startGopayCheckout(_ sender: Any) {
        let paymentVC = UIStoryboard(name: "Main", bundle: nil).instantiateViewController(identifier: "paymentVC") as PaymentViewController
        paymentVC.phoneNumber = self.phoneNumberTF.text ?? ""
        self.navigationController?.pushViewController(paymentVC, animated: true)
    }
}
