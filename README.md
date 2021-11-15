## Introduction

In the cafeteria of the Freie Waldorfschule Augsburg (FWA) there is a large coffee machine from the company Franke Kaffeemaschinen AG. 
During the breaks, students can buy a coffee, hot water for a tea or milk there. Payment is made with coins. 
This machine communicates with a coin counter right next to it. 

Since hardly anyone carries around coinage these days, the idea came up to have the coffee machine interact with the recently introduced MensaMax RFID chips.

> **And what can I say... we did it!**

## Milestones

This project consists of two major milestones:

1. The automated communication to MensaMax 
2. The automated communication with the coffee machine via MDB.

### MensaMax

For communicating with MensaMax, we developed the open-source programming interface ["Clerk"](https://github.com/FreieWaldorfschuleAugsburg/clerk). It enables direct and automated interaction with MensaMax.
Drinks booked via the coffee machine are linked to their corresponding product within MensaMax's kiosk and automatically billed.

### MDB

The coffee vending machine communicates with its "payment methods" via a standardised vending machine protocol: MDB.
The company Qibixx produces attachments for the Raspberry Pi that enable MDB communication by means of a serial interface.

## Conclusion

This system allows quick and easy payment at the coffee machine, without long searching for the right coins, etc.
> If you have any suggestions for improvement, please [feel free to let me know](https://elektronisch.dev/kontakt)!
