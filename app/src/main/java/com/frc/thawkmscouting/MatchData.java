package com.frc.thawkmscouting;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.util.HashMap;

/**
 * The schema file in the form of a class used to format data before pushing to AWS
 */
@DynamoDBTable(tableName = "WeekOne")
public class MatchData {
    private Integer m_team;
    private Integer m_match;
    private String m_color;
    private Integer m_driverStation;
    private Boolean m_crossedLine;
    private HashMap<String, Integer> m_autoHits;
    private HashMap<String, Integer> m_autoMiss;
    private Integer m_timePlayingDefense;
    private Integer m_timeDefenseOnTeam;
    private Integer m_penalties;
    private HashMap<String, HashMap<String, Integer>> m_scoring = new HashMap<>();
    private Boolean m_rotationControl;
    private Boolean m_colorControl;
    private Boolean m_attemptedClimb;
    private Boolean m_climb;
    private Boolean m_level;
    private Boolean m_attemptedDoubleClimb;
    private Boolean m_brownedOut;
    private Boolean m_disabled;
    private Boolean m_yellowCard;
    private Boolean m_redCard;
    private String m_name;
    private String m_notes;

    @DynamoDBHashKey(attributeName = "Team")
    public Integer getTeam() { return m_team; }
    void setTeam(final Integer _team) { this.m_team = _team; }

    @DynamoDBRangeKey (attributeName = "Match")
    public Integer getMatch() { return m_match; }
    void setMatch(final Integer _match) { this.m_match = _match; }

    @DynamoDBAttribute(attributeName = "Color")
    public String getColor() { return m_color; }
    void setColor(final String _color) { this.m_color = _color; }

    @DynamoDBAttribute(attributeName = "Driver Station")
    public Integer getDriverStation() { return m_driverStation; }
    void setDriverStation(final Integer _driverStation) { this.m_driverStation = _driverStation; }

    @DynamoDBAttribute(attributeName = "Crossed Line")
    public Boolean getCrossedLine() { return m_crossedLine; }
    void setCrossedLine(final Boolean _crossedLine) { this.m_crossedLine = _crossedLine; }

    @DynamoDBAttribute(attributeName = "Auto Hits")
    public HashMap<String, Integer> getAutoHits() { return m_autoHits; }
    void setAutoHits(final HashMap<String, Integer> _autoHits) { this.m_autoHits = _autoHits; }

    @DynamoDBAttribute(attributeName = "Auto Miss")
    public HashMap<String, Integer> getAutoMiss() { return m_autoMiss; }
    void setAutoMiss(final HashMap<String, Integer> _autoMiss) { this.m_autoMiss = _autoMiss; }

    @DynamoDBAttribute(attributeName = "Time Playing Defense")
    public Integer getTimePlayingDefense() { return m_timePlayingDefense; }
    void setTimePlayingDefense(final Integer _time) { this.m_timePlayingDefense = _time; }

    @DynamoDBAttribute(attributeName = "Time Defense On Team")
    public Integer getTimeDefenseOnTeam() { return m_timeDefenseOnTeam; }
    void setTimeDefenseOnTeam(final Integer _time) { this.m_timeDefenseOnTeam = _time; }

    @DynamoDBAttribute(attributeName = "Penalties")
    public Integer getPenalties() { return m_penalties; }
    void setPenalties(final Integer _penalties) { this.m_penalties = _penalties; }

    @DynamoDBAttribute(attributeName = "Scoring")
    public HashMap<String, HashMap<String, Integer>> getScoring() { return m_scoring; }
    void setScoring(final HashMap<String, HashMap<String, Integer>> _scoring) { this.m_scoring = _scoring; }

    @DynamoDBAttribute(attributeName = "Rotation Control")
    public boolean getRotationControl() { return m_rotationControl; }
    void setRotationControl(final boolean _rotationControl) { this.m_rotationControl = _rotationControl; }

    @DynamoDBAttribute(attributeName = "Color Control")
    public boolean getColorControl() { return m_colorControl; }
    void setColorControl(final boolean _colorControl) { this.m_colorControl = _colorControl; }

    @DynamoDBAttribute(attributeName = "Attempted Climb")
    public boolean getAttemptedClimb() { return m_attemptedClimb; }
    void setAttemptedClimb(final boolean _attemptedClimb) { this.m_attemptedClimb = _attemptedClimb; }

    @DynamoDBAttribute(attributeName = "Climb")
    public boolean getClimb() { return m_climb; }
    void setClimb(final boolean _climb) { this.m_climb = _climb; }

    @DynamoDBAttribute(attributeName = "Level")
    public boolean getLevel() { return m_level; }
    void setLevel(final boolean _level) { this.m_level = _level; }

    @DynamoDBAttribute(attributeName = "Attempted Double Climb")
    public boolean getAttemptedDoubleClimb() { return m_attemptedDoubleClimb; }
    void setAttemptedDoubleClimb(final boolean _attemptedDoubleClimb) { this.m_attemptedDoubleClimb = _attemptedDoubleClimb; }

    @DynamoDBAttribute(attributeName = "Double Climb")
    public boolean getDoubleClimb() { return m_attemptedClimb; }
    void setDoubleClimb(final boolean _doubleClimb) { this.m_climb = _doubleClimb; }

    @DynamoDBAttribute(attributeName = "Browned Out")
    public boolean getBrownedOut() { return m_brownedOut; }
    void setBrownedOut(final boolean _brownedOut) { this.m_brownedOut = _brownedOut; }

    @DynamoDBAttribute(attributeName = "Disabled")
    public boolean getDisabled() { return m_disabled; }
    void setDisabled(final boolean _disabled) { this.m_disabled = _disabled; }

    @DynamoDBAttribute(attributeName = "Red Card")
    public boolean getRedCard() { return m_redCard; }
    void setRedCard(final boolean _redCard) { this.m_redCard = _redCard; }

    @DynamoDBAttribute(attributeName = "Yellow Card")
    public boolean getYellowCard() { return m_yellowCard; }
    void setYellowCard(final boolean _yellowCard) { this.m_yellowCard = _yellowCard; }

    @DynamoDBAttribute(attributeName = "Scouter Name")
    public String getName() { return m_name; }
    void setName(final String _name) { this.m_name = _name; }

    @DynamoDBAttribute(attributeName = "Notes")
    public String getNotes() { return m_notes; }
    void setNotes(final String _notes) { this.m_notes = _notes; }
}
