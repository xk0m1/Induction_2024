# Induction_2024

LIVE DEMO (WHOEVER IS PRSENTING THIS... PLEASE TRY TO INSTALL THIS APP IN YOUR PHONES 
           TO KNOW ABOUT THIS APP BETTER)

### 1. DIFFERENT ACCESSIBILITY EVENTS USED IN THIS MALICIOUS APPLICATION

- `TYPE_VIEW_CLICKED` - This event is triggered when a view is clicked. 
                      When you click a button in an app, this event is fired.
                                                          
- `TYPE_VIEW_FOCUSED` - This event is fired when a view gains focus.
                      When you tap on a text field to start typing.
                      
  - **Focus on the "EditText" labels - example - DIVA APK and whatsapp texts**
                    
- `TYPE_VIEW_LONG_CLICKED` - This event occurs when a user long-clicks a view.
                           When you long-press a button to bring up additional options.
                         
- `TYPE_VIEW_SCROLLED` - This event happens when a user scrolls a view.
                       When you scroll through a list of contacts.
                     
- `TYPE_VIEW_TEXT_CHANGED` - This event occurs when the text in a view changes. When you type a message in a chat app.
                           
**It's the GOD !!! The Keylogger works for all apps. (The content inside the browsers is not getting logged)**


### 2. IMPORTANT STUFF TO KNOW ABOUT THE CODE/APP

- Accessibility permission required or else app won't work (Code of that is there in theMainActivity)
   
- The service will work even if the app is closed ( so .. once it is installed and the 
  permission is given ... the mobile is under our control )

- `TYPE_VIEW_CLICKED`

  The logs regarding to this is available in package:mine "data"

  Any app click.... (All types of elements in the app { APP, TextView, Button etc ... }
  Exceptions :
  
  discord, Telegram, Messages app..... text messages ... when clicked wont show up
  
  INSTAGRAM ---- SOMETIMES WILL WORK SOMETIMES IT WON'T ..... ONE TRICK 
                 LONG CLICK OF INSTAGRAM IS CONSIDERED AS A SINGLE CLICK ( MESSAGE
                 WILL BE DISPLAYED )
        
  MESSAGES  ---- TO SEE THE MESSAGE OF THE MESSAGES APP..... LONG CLICK ON THE 
                 GROUP MESSAGE... THE LAST SENT MESSAGE WILL BE DISPLAYED.
                 
  The elements that we click on after opening the website won't work as well....
  becz that doesn't belong to the app.... it belongs to the website

  INITIALLY : 
  NORMAL APP CLICKS-- (not working for apps like chrome, messages and photos)

  But ... i solved tht issue ... with this line of code

  ```Log.d("data", "App name: " + event.getContentDescription());```

  This uses the pre-built method getContentDescription of the AccessibilityEvent class
  
- `TYPE_VIEW_LONG_CLICKED`
  
  The logs regarding to this is available in package:mine "data"

  INSTAGRAM ---- SOMETIMES WILL WORK SOMETIMES IT WON'T ..... ONE TRICK 
                 LONG CLICK OF INSTAGRAM IS CONSIDERED AS A SINGLE CLICK ( MESSAGE
                 WILL BE DISPLAYED )
                 
  Any app that is long clicked... will be shown ( long click only works for apps {as far
  as i know } )
  
- `TYPE_VIEW_FOCUSED`
 
  The logs regarding to this is available in package:mine "data"

  In apps like Diva, Whatsapp, Discord ... and many other apps.... when a certain 
  activity is opened the cursor directly goes to the EditText box ..... In such cases
  the text "view focused" and the EditText description ( if there is any ) is 
  displayed.
  
- `TYPE_VIEW_SCROLLED`

  The logs regarding to this is available in package:mine "ScrollEvent"
  
  The logs of each and every scroll ... is recorded....
  Important info
  
  Event Type : TYPE_VIEW_SCROLLED (for our reference)
  Package Name : name of the package in which we are scrolling
  Class Name : What kind of View is it ( List View / Recycler View )
  Item Count : Number of items in the list
  
  Even .. if we delete an ite, from the list .. the list is immediately updated.
  
- `TYPE_VIEW_TEXT_CHANGED`

  The logs regarding to this is available in package:mine "data"
  
  It's the GOD !!! The keylogger.
  
  Each and every letter that we type in any view... is logged . (If we type slowly)
  If the word is typed fast..... then there are chances that few live updtes are missing
  
- `TYPE_NOTIFICATION_STATE_CHANGED`
 
   The logs regarding to this is available in package:mine "notification"
 
   Each and every notification that we receive and appears on top of our phone is logged.
   The notifications of apps .. that have (notification permissions)
   
   Important data that we reciever from this:
   
   . SENDER  (main text)
   . The text that he sent (sub text)
   
   
### 3. FINALLY
  
  - AndroidManifest contains this code which is the most important for the type of service that is running
  
    ```android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE">```
  
  - All the accessibility events are in the accessibility_service_config.xml

    ```android:accessibilityEventTypes="typeAllMask"```
  
  
- THINGS THAT CAN WOW THE CROWD (IF THERE IS ANYTHING ELSE ... WE CAN ADD)

  - Keylogger
  - Bank Details
  - UPI 
  - Phone Password ( only if alphanumeric )
  - Messages
  - Images ( date and time of when it was stored )
  - gmails etc 

### 4. Latest Update

- We are processing NodeInfo. Consider Nodes as the individual element of a Screen at a particular moment
- This gives additional information which wasn't available from earlier events

  
  
   
   
  
  
  
  

  
                 
  

  
  
  







                         
                         
                         
                     
                         
                            
                            




