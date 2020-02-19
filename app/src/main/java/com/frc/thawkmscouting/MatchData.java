package com.frc.thawkmscouting;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@DynamoDBTable(tableName = "EventData")
public class MatchData {
    private String team;
    private String match;
    private String color;
    private Integer driverStation;
    private HashMap<String, Integer> autoHits;
    private HashMap<String, Integer> autoMiss;
    private Integer timePlayingDefense;
    private Integer timeDefenseOnTeam;
    private Integer penalties;
    private HashMap<String, HashMap<String, Integer>> scoring = new HashMap<>();
    private Boolean rotationControl;
    private Boolean colorControl;
    private Boolean attemptedClimb;
    private Boolean climb;
    private Boolean level;
    private Boolean attemptedDoubleClimb;
    private Boolean brownedOut;
    private Boolean disabled;
    private Boolean yellowCard;
    private Boolean redCard;
    private String notes;

    @DynamoDBHashKey(attributeName = "Teams")
    public String getTeam() { return team; }
    void setTeam(final String _team) {
        this.team = _team;
    }

    @DynamoDBRangeKey (attributeName = "Match")
    public String getMatch() { return match; }
    void setMatch(final String _match) {
        this.match = _match;
    }

    @DynamoDBAttribute(attributeName = "Color")
    public String getColor() { return color; }
    void setColor(final String _color) { this.color = _color; }

    @DynamoDBAttribute(attributeName = "Driver Station")
    public Integer getDriverStation() { return driverStation; }
    void setDriverStation(final Integer _driverStation) { this.driverStation = _driverStation; }

    @DynamoDBAttribute(attributeName = "Auto Hits")
    public HashMap<String, Integer> getAutoHits() { return autoHits; }
    void setAutoHits(final HashMap<String, Integer> _autoHits) { this.autoHits = _autoHits; }

    @DynamoDBAttribute(attributeName = "Auto Miss")
    public HashMap<String, Integer> getAutoMiss() { return autoMiss; }
    void setAutoMiss(final HashMap<String, Integer> _autoMiss) { this.autoMiss = _autoMiss; }

    @DynamoDBAttribute(attributeName = "Time Playing Defense")
    public Integer getTimePlayingDefense() { return timePlayingDefense; }
    void setTimePlayingDefense(final Integer _time) { this.timePlayingDefense = _time; }

    @DynamoDBAttribute(attributeName = "Time Defense On Team")
    public Integer getTimeDefenseOnTeam() { return timeDefenseOnTeam; }
    void setTimeDefenseOnTeam(final Integer _time) { this.timeDefenseOnTeam = _time; }

    @DynamoDBAttribute(attributeName = "Penalties")
    public Integer getPenalties() { return penalties; }
    void setPenalties(final Integer _penalties) { this.penalties = _penalties; }

    @DynamoDBAttribute(attributeName = "Scoring")
    public HashMap<String, HashMap<String, Integer>> getScoring() { return scoring; }
    void setScoring(final HashMap<String, HashMap<String, Integer>> _scoring) { this.scoring = _scoring; }

    @DynamoDBAttribute(attributeName = "Rotation Control")
    public boolean getRotationControl() { return rotationControl; }
    void setRotationControl(final boolean _rotationControl) { this.rotationControl = _rotationControl; }

    @DynamoDBAttribute(attributeName = "Color Control")
    public boolean getColorControl() { return colorControl; }
    void setColorControl(final boolean _colorControl) { this.colorControl = _colorControl; }

    @DynamoDBAttribute(attributeName = "Attempted Climb")
    public boolean getAttemptedClimb() { return attemptedClimb; }
    void setAttemptedClimb(final boolean _attemptedClimb) { this.attemptedClimb = _attemptedClimb; }

    @DynamoDBAttribute(attributeName = "Climb")
    public boolean getClimb() { return climb; }
    void setClimb(final boolean _climb) { this.climb = _climb; }

    @DynamoDBAttribute(attributeName = "Level")
    public boolean getLevel() { return level; }
    void setLevel(final boolean _level) { this.level = _level; }

    @DynamoDBAttribute(attributeName = "Attempted Double Climb")
    public boolean getAttemptedDoubleClimb() { return attemptedDoubleClimb; }
    void setAttemptedDoubleClimb(final boolean _attemptedDoubleClimb) { this.attemptedDoubleClimb = _attemptedDoubleClimb; }

    @DynamoDBAttribute(attributeName = "Double Climb")
    public boolean getDoubleClimb() { return attemptedClimb; }
    void setDoubleClimb(final boolean _doubleClimb) { this.climb = _doubleClimb; }

    @DynamoDBAttribute(attributeName = "Browned Out")
    public boolean getBrownedOut() { return brownedOut; }
    void setBrownedOut(final boolean _brownedOut) { this.brownedOut = _brownedOut; }

    @DynamoDBAttribute(attributeName = "Disabled")
    public boolean getDisabled() { return disabled; }
    void setDisabled(final boolean _disabled) { this.disabled = _disabled; }

    @DynamoDBAttribute(attributeName = "Red Card")
    public boolean getRedCard() { return redCard; }
    void setRedCard(final boolean _redCard) { this.redCard = _redCard; }

    @DynamoDBAttribute(attributeName = "Yellow Card")
    public boolean getYellowCard() { return yellowCard; }
    void setYellowCard(final boolean _yellowCard) { this.yellowCard = _yellowCard; }

    @DynamoDBAttribute(attributeName = "Notes")
    public String getNotes() { return notes; }
    void setNotes(final String _notes) { this.notes = _notes; }
}
