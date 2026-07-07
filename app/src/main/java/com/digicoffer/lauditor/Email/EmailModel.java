package com.digicoffer.lauditor.Email;

public class EmailModel {


    private String id;
    private String name;
    private String type;
    private int messagesTotal;
    private int messagesUnread;
    private int threadsTotal;
    private String isauthaccess;
    private int threadsUnread;
    private String emailsVM;

    public static Object getInstance() {
        return null;
    }
    public String getIsauthaccess(){
        return isauthaccess;
    }
    public void setIsauthaccess(){
        this.isauthaccess = isauthaccess;
    }


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public int getMessagesTotal() {
            return messagesTotal;
        }

        public void setMessagesTotal(int messagesTotal) {
            this.messagesTotal = messagesTotal;
        }

        public int getMessagesUnread() {
            return messagesUnread;
        }

        public void setMessagesUnread(int messagesUnread) {
            this.messagesUnread = messagesUnread;
        }

        public int getThreadsTotal() {
            return threadsTotal;
        }

        public void setThreadsTotal(int threadsTotal) {
            this.threadsTotal = threadsTotal;
        }

        public int getThreadsUnread() {
            return threadsUnread;
        }

        public void setThreadsUnread(int threadsUnread) {
            this.threadsUnread = threadsUnread;
        }





    }


