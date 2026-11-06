package com.example.listprocessor;

import java.util.LinkedList;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ItemList extends LinkedList<Cruise> {
    @Inject
    ItemList() { }
}
