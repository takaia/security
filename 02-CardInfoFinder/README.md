# Assignment 2

The current version of the Point of Sale (PoS) system does not require cardholder data to be encrypted when they are saved in non-persistent memory (e.g. RAM). This design flaw, unfortunately, allows attackers to install malware on a PoS system to steal the credit card information of cardholders. In 2005, “Target Corp. was hit by an extensive theft of its customers' credit-card and debit-card data over the busy Black Friday weekend”[1].

In this assignment, you are asked to implement a Java program to validate if a Point of Sale (PoS) system has unencrypted credit card track I data in memory. A sample memory data of a PoS system is provided, called memorydump.dmp. Although the memory data can be obtained via existing memory dump tools, this sample file is hand-made.

[1]: http://www.wsj.com/articles/SB10001424052702304773104579266743230242538
