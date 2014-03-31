##Restaurant Project Repository

###Student Information
  + Name: Gueho Choi
  + USC Email: guehocho@usc.edu
  + USC ID: 8507910356
  + Section: Lecture, 30303; Lab, 30134;

###Resources
  + [Restaurant v1](http://www-scf.usc.edu/~csci201/readings/restaurant-v1.html)
  + [Agent Roadmap](http://www-scf.usc.edu/~csci201/readings/agent-roadmap.html)
  
###Instructions for compiling/running code.
  + If you try to run it on eclipse, just create new "java" project, on the panel, uncheck Use default location box, 
    and then direct yourself to the directory the project is in, then eclipse will automatically configure rest of it, 
    just click on finish. This way, you will not have "no previous running" error.
  + If you know how to import/setup existing java project in Eclipse in diffrent way, please do so.

###Instructions to test code 

  + (includes hacks < see ListPanel.java for hack implementations >)

  + Normative Scenarios
  1. One of every type of agent, no market interactions, customer orders, pays, and leaves
    + add waiter, and customer, set customer hungry
  2. No customers, cook orders low items from market, when food arrives, then customers arrive
    + When starting up the application. The cook will fill up 5 beef which has only 2 left. After it fills the low inventories, it opens the restaurant.
  3. Multiple customers, multiple waiters, cashier operating normally, no running out of food. 
    + every inventories are filled up with 10. Therefore, up to at least 10 customers work fine w/o running out of food.
  4. Waiter wants to go on break, he's told it's ok, goes on break when he finished all his current customers; goes on break, then goes off break.
    + click on the waiter on the list. Then, select the checkbox WantABreak? if there are more than two waiters, he will go on break. Note that my design made it unnecssary for waiter to receive confirmation Agent message. Rather, the host simply won't assign anymore customers to the waiter. He resumes to work if backToWork checkbox is checked. Trivial testing scenario. Add 1 waiter, try going on break. Add 1 more waiter, try going on break. Resume to work. Add 2 customers. Go on break, then only one of them is allowed. The waiter going on break will finish all customers assigned already first before he goes on the break.
  5. One order, fulfilled by the market, bill paid in full.
    + By default, restaurant has enough money to pay, I did not implement hacks to test this because I am providing JUnit tests. However, you can see this normative case easily as you start the application because cook orders low items when opening restaurant.
    

  + Non-normative Scenarios
  1. Customer orders, food has run out, cook has obviously ordered but food hasn't arrived, Customer makes new choice.
    + Add many customers named "Chicken"
  2. Cook orders from a Market, but is told they can't fulfill his order; must order from one of his backup markets.
    + My design sends out order to ALL markets. Then, cook confirms the delivery depending on item is already filled or not. 
  3. Waiter wants to go on break, he's told it's NOT OK and must keep working.
    + My design will NOT tell him to keep working. Host is mean. He will just keep assign customers.
  4. Customer comes to restaurant and restaurant is full, customer is told and waits.
    + By default, customers wait. Just add more than 5 customers.
  5. Customer comes to restaurant and restaurant is full, customer is told and leaves.
    + Restart application. When any of textfield on lists FOCUSED, use CTRL + 0 hack to add 5 waiters and 7 customers. The added customers will be set to be hungry and leaving restaurant if full automatically. After this test, the application should be restarted since agents created using hacks might not be reusable by clicking the button (ex. setting hungry again). Look for console message saying "I am not waiting, good bye"  
  6. Customer doesn't have enough money to order anything and leaves.
    + Make sure any of text field is focused (better if you focused on customer text field). Use CTRL + 1 hack to add a csutomer with only $1.
  7. Customer has only enough money to order the cheapest item.
    + Make sure any of text field is focused (better if you focused on customer textfield). Use CTRL + 2 hack to add a customer with only $10. 
  8. Customer has only enough money to order the cheapest item; he orders it, then they run out of it and he leaves.
    + Use CTRL + 3 hack to make cook to set chicken stock to be 0. Then, add $10 customer using CTRL + 2 hack.
  9. Customer orders, eats, but hasn't enough money to pay.
    + Make sure customer's textField is focused before using this hack. Use CTRL + 4 hack to add a customer with customer with $1 who still orders food. After he leaves upon promise that he will pay next time, you can set him to hungry again. You will see him being kicked out of restaurant. After that, you can use CTRL + 5 hack to make all customers have $100. Then set the indecent customer hungry again. He will pay the debt and sit on the table. (for this case, gui is not well supported, but it is not requirement)
  10. One order, fulfilled by TWO markets, 2 bills paid in full.
    + Note that hack is not implemented. However, one way to test it manually is to change the 361:CookAgent.java (cookAgent constructor, change restockAmount from 5 to 10). Because all markets are stocked up with 5 beefs in default, 10 restock amount will result in ordering from two or more markets.  

###A description of what doesn't work.
  + I have not implemented hacks to test the ordering because we are to implement the JUnit tests.
  + No customers, cook orders low items from market, when food arrives, then customers arrive. This scenario works fine only for a few orders. That is, if I set many items low, then it behaves unpredictably. It seems like something to do with synchronized keyword since it works little better when i remove few of them. It seems it takes max 3 items. But, it works if it's not opening. 


###General
  + NOTE in V 2.2
  + Waiter's home position is upper-right corner (maximum 30, if exceeds 30, then they are all placed on the same upper-right-most corner)
  + Customer's waiting area is a little above the lower right corner (maximum 30, if exceeds 30, then they are all placed on the same upper-right-most corner)
  + Cashier's position ( where customer pays bill ) is lower right corner (below customer's waiting area)
  + CookGui queries KitchenGui for the position of objects (refrigerator, cooking area, plating area)
  + KitchenGui's plating area is queried by WaiterGui too.
  + Kitchen configuration: Kitchen (Light-Gray), Refrigerator (BLUE), Cooking Area(Dark-Gray), Plating Area(WHITE)


  + NOTE in V 2.1
  + Starting up (Opening restaurant) takes about 10 seconds. After you are done with the normative scenario, you should change the lines 141-145:MarketAgent.java from 10000 to desirable delivery speed.
  + Scenario 1: A customer must have enough money to pay for the ordered item. If customer cannot pay for the check,
    cashier tells the customer that he can pay on next visit. When they visit next time, the host checks the history
    of customers and tells him he has deferred payment to make. The customer goest to the cashier and pays the deferred bill.
    If he again have no money to pay, then he gets kicked out from the restaurant, else, the normal scenario goes on.
  + Prices : Chicken - 5.49, Beef - 12.99, Turkey - 11.99, Pork - 10.99, Duck - 16.99
  + For all prices, taxes will be added when check is issued. When customer checks for the prices if they can afford, they think about taxes too.
  + All customers start with 100 cash.
  + HACK USE: Note that hacks are not completely working. They are added into the lists, but may not be able to select them and make them hungry again or go on break. You should restart after using hacks.
  + All foods start with 10 stock amount, 4 as low threshold, 10 as restocking amount.


  + NOTE in V 2.0
  + Please note that there is mainly two global vairables that I created. 
    1. NTABLES in HostAgent: This is shared with Gui elements (mostly read-only).
    2. tableMap in AnimationPanel: This is mapped uppon AnimationPanel's instantiation, and shared with WatierGui.
  + I have not implemented with icons yet, but the strings to represent the foods are implemented. 
  + For the purpose of debugging, I have implemented professor Wilczynski's suggestion which is to let user to input
    number to add number of customers. I allow up to 9 customers adding at once. No restriction applies to add more.
  + Waiter's panel has a check box too. This checkbox is not implemented yet. However, it is not removed becuase I will
    implement the "agent at work" for that checkbox, so that we can add waiter who is not at work yet.
  + Customer Agent will send message after 10 seconds (this is hardcoded as timer scheduling).

  