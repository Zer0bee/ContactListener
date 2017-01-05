# Android ContactListener

This library will let you know which contact has been added, updated or deleted in your contact since your last sync. 

**Note:** In this lib we are not using contact version name, that is lengthy and time taking process so we are using hash on contact. 


**Usage**

        //Call update() method everytime if you wanna check for add, delete or update
        HashMap<Integer, HashSet<Integer>> update = ContactHelper.getInstance(this).update();
        
        Iterator<Map.Entry<Integer, HashSet<Integer>>> iterator = update.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, HashSet<Integer>> next = iterator.next();
            Integer key = next.getKey();
            if (key == ContactHelper.CONTACT_ADDED) {
                //Set of all added contacts
            } else if (key == ContactHelper.CONTACT_DELETED) {
                //Set of all deleted contacts
            } else if (key == ContactHelper.CONTACT_UPDATED) {
                //Set of all updated contacts
            }
        }