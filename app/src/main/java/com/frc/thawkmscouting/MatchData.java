package com.frc.thawkmscouting;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.util.HashMap;

/**
 * The schema file used to format data before pushing to AWS
 *
 * @author Aniketh Dandu - FRC Team 1100
 */
@DynamoDBTable(tableName = BuildConfig.TABLENAME)
class MatchData {
    private int m_team;
    private int m_match;
    private String m_color;
    private int m_driverStation;
    private boolean m_crossedLine;
    private HashMap<String, Integer> m_autoHits;
    private HashMap<String, Integer> m_autoMiss;
    private int m_timePlayingDefense;
    private int m_timeDefenseOnTeam;
    private int m_penalties;
    private HashMap<String, HashMap<String, Integer>> m_scoring = new HashMap<>();
    private boolean m_rotationControl;
    private boolean m_colorControl;
    private boolean m_attemptedClimb;
    private boolean m_climb;
    private boolean m_level;
    private boolean m_attemptedDoubleClimb;
    private boolean m_brownedOut;
    private boolean m_disabled;
    private boolean m_yellowCard;
    private boolean m_redCard;
    private String m_name;
    private String m_notes;

    // **************************************************
    // Set methods
    // **************************************************

    // All set methods used to set a value in the MatchData object

    void setTeam(final int _team) {
        this.m_team = _team;
    }

    void setMatch(final int _match) {
        this.m_match = _match;
    }

    void setColor(final String _color) {
        this.m_color = _color;
    }

    void setDriverStation(final int _driverStation) {
        this.m_driverStation = _driverStation;
    }

    void setCrossedLine(final boolean _crossedLine) {
        this.m_crossedLine = _crossedLine;
    }

    void setAutoHits(final HashMap<String, Integer> _autoHits) {
        this.m_autoHits = _autoHits;
    }

    void setAutoMiss(final HashMap<String, Integer> _autoMiss) {
        this.m_autoMiss = _autoMiss;
    }

    void setTimePlayingDefense(final int _time) {
        this.m_timePlayingDefense = _time;
    }

    void setTimeDefenseOnTeam(final int _time) {
        this.m_timeDefenseOnTeam = _time;
    }

    void setPenalties(final int _penalties) {
        this.m_penalties = _penalties;
    }

    void setScoring(final HashMap<String, HashMap<String, Integer>> _scoring) {
        this.m_scoring = _scoring;
    }

    void setRotationControl(final boolean _rotationControl) {
        this.m_rotationControl = _rotationControl;
    }

    void setColorControl(final boolean _colorControl) {
        this.m_colorControl = _colorControl;
    }

    void setAttemptedClimb(final boolean _attemptedClimb) {
        this.m_attemptedClimb = _attemptedClimb;
    }

    void setClimb(final boolean _climb) {
        this.m_climb = _climb;
    }

    void setLevel(final boolean _level) {
        this.m_level = _level;
    }

    void setAttemptedDoubleClimb(final boolean _attemptedDoubleClimb) {
        this.m_attemptedDoubleClimb = _attemptedDoubleClimb;
    }

    void setDoubleClimb(final boolean _doubleClimb) {
        this.m_climb = _doubleClimb;
    }

    void setBrownedOut(final boolean _brownedOut) {
        this.m_brownedOut = _brownedOut;
    }

    void setDisabled(final boolean _disabled) {
        this.m_disabled = _disabled;
    }

    void setRedCard(final boolean _redCard) {
        this.m_redCard = _redCard;
    }

    void setYellowCard(final boolean _yellowCard) {
        this.m_yellowCard = _yellowCard;
    }

    void setName(final String _name) {
        this.m_name = _name;
    }

    void setNotes(final String _notes) {
        this.m_notes = _notes;
    }

    // **************************************************
    // Get methods
    // **************************************************

    // All get methods used to return a value stored in the MatchData object

    @DynamoDBHashKey(attributeName = "Team")
    int getTeam() {
        return m_team;
    }

    @DynamoDBRangeKey(attributeName = "Match")
    int getMatch() {
        return m_match;
    }

    @DynamoDBAttribute(attributeName = "Color")
    String getColor() {
        return m_color;
    }

    @DynamoDBAttribute(attributeName = "Driver Station")
    int getDriverStation() {
        return m_driverStation;
    }

    @DynamoDBAttribute(attributeName = "Crossed Line")
    boolean getCrossedLine() {
        return m_crossedLine;
    }

    @DynamoDBAttribute(attributeName = "Auto Hits")
    HashMap<String, Integer> getAutoHits() {
        return m_autoHits;
    }

    @DynamoDBAttribute(attributeName = "Auto Miss")
    HashMap<String, Integer> getAutoMiss() {
        return m_autoMiss;
    }

    @DynamoDBAttribute(attributeName = "Time Playing Defense")
    int getTimePlayingDefense() {
        return m_timePlayingDefense;
    }

    @DynamoDBAttribute(attributeName = "Scoring")
    HashMap<String, HashMap<String, Integer>> getScoring() {
        return m_scoring;
    }

    @DynamoDBAttribute(attributeName = "Time Defense On Team")
    int getTimeDefenseOnTeam() {
        return m_timeDefenseOnTeam;
    }

    @DynamoDBAttribute(attributeName = "Penalties")
    int getPenalties() {
        return m_penalties;
    }

    @DynamoDBAttribute(attributeName = "Rotation Control")
    boolean getRotationControl() {
        return m_rotationControl;
    }

    @DynamoDBAttribute(attributeName = "Color Control")
    boolean getColorControl() {
        return m_colorControl;
    }

    @DynamoDBAttribute(attributeName = "Attempted Climb")
    boolean getAttemptedClimb() {
        return m_attemptedClimb;
    }

    @DynamoDBAttribute(attributeName = "Climb")
    boolean getClimb() {
        return m_climb;
    }

    @DynamoDBAttribute(attributeName = "Level")
    boolean getLevel() {
        return m_level;
    }

    @DynamoDBAttribute(attributeName = "Attempted Double Climb")
    boolean getAttemptedDoubleClimb() {
        return m_attemptedDoubleClimb;
    }

    @DynamoDBAttribute(attributeName = "Double Climb")
    boolean getDoubleClimb() {
        return m_attemptedClimb;
    }

    @DynamoDBAttribute(attributeName = "Browned Out")
    boolean getBrownedOut() {
        return m_brownedOut;
    }

    @DynamoDBAttribute(attributeName = "Disabled")
    boolean getDisabled() {
        return m_disabled;
    }

    @DynamoDBAttribute(attributeName = "Red Card")
    boolean getRedCard() {
        return m_redCard;
    }

    @DynamoDBAttribute(attributeName = "Yellow Card")
    boolean getYellowCard() {
        return m_yellowCard;
    }

    @DynamoDBAttribute(attributeName = "Scouter Name")
    String getName() {
        return m_name;
    }

    @DynamoDBAttribute(attributeName = "Notes")
    String getNotes() {
        return m_notes;
    }
}