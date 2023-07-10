package com.jedmahonisgroup.gamepoint.model;

import java.util.Objects;

public class DayList {
        private long id;
        private String timeStamp;

        public DayList(long id, String name) {
            this.id = id;
            this.timeStamp = name;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getTimeStamp() {
            return timeStamp;
        }

        public void setTimeStamp(String timeStamp) {
            this.timeStamp = timeStamp;
        }

        // Two customers are equal if their IDs are equal
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DayList customer = (DayList) o;
            return timeStamp.equals(customer.timeStamp);
        }

        @Override
        public int hashCode() {
            return Objects.hash(timeStamp);
        }
//
//    @Override
//    public String toString() {
//        return "Customer{" +
//                "id=" + timeStamp +
//                ", timeStamp='" + timeStamp + '\'' +
//                '}';
//    }
    }

//public class HashSetUserDefinedObjectExample {
//    public static void main(String[] args) {
//        Set<TestModel> customers = new HashSet<>();
//        customers.add(new TestModel(101, "Rajeev"));
//        customers.add(new TestModel(102, "Sachin"));
//        customers.add(new TestModel(103, "Chris"));
//
//        /*
//          HashSet will use the `equals()` & `hashCode()` implementations
//          of the Customer class to check for duplicates and ignore them
//        */
//        customers.add(new Customer(101, "Rajeev"));
//
//        System.out.println(customers);
//    }
//}













