package com.spendcheck.model;

public enum Category {
    TRAVEL("Travel"),
    EDUCATION("Education"),
    FOOD("Food"),
    ESSENTIALS("Essentials"),
    OTHER("Other");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Category fromDisplayName(String name) {
        for (Category category : values()) {
            if (category.displayName.equalsIgnoreCase(name) || category.name().equalsIgnoreCase(name)) {
                return category;
            }
        }
        return OTHER;
    }
}
