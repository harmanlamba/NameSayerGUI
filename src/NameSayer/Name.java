package NameSayer;

import java.util.Map;
import java.util.HashMap;;
import java.util.Date;

public class Name {

    private String _name;
    private Map<Date,Recording> _versions = new HashMap<>();
    private Map<Date,Recording> _attempts = new HashMap<>();

    public Name(String name) {
        _name = name;
    }

}
