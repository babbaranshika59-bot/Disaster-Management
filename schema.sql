-- RescueGrid database (optional: the program can also create this from menu 11 -> option 2)
CREATE DATABASE IF NOT EXISTS rescuegrid_db;
USE rescuegrid_db;

CREATE TABLE IF NOT EXISTS users (
    user_id INT PRIMARY KEY, name VARCHAR(60) NOT NULL, phone VARCHAR(15),
    username VARCHAR(30) NOT NULL UNIQUE, password VARCHAR(30) NOT NULL, role VARCHAR(20) NOT NULL);

CREATE TABLE IF NOT EXISTS incidents (
    incident_id INT PRIMARY KEY, disaster_type VARCHAR(30) NOT NULL, location VARCHAR(60) NOT NULL,
    description VARCHAR(200), affected_people INT, severity INT, status VARCHAR(15),
    reported_at VARCHAR(40), extra VARCHAR(60));

CREATE TABLE IF NOT EXISTS rescue_teams (
    team_id INT PRIMARY KEY, team_name VARCHAR(40) NOT NULL, specialization VARCHAR(30),
    available BOOLEAN, assigned_incident_id INT);

CREATE TABLE IF NOT EXISTS team_members (
    member_id INT PRIMARY KEY, team_id INT NOT NULL, member_name VARCHAR(60), skill VARCHAR(30),
    FOREIGN KEY (team_id) REFERENCES rescue_teams(team_id));

CREATE TABLE IF NOT EXISTS resources (
    resource_id INT PRIMARY KEY, name VARCHAR(40) NOT NULL, category VARCHAR(20),
    total_qty INT, allocated_qty INT);

CREATE TABLE IF NOT EXISTS relief_camps (
    camp_id INT PRIMARY KEY, name VARCHAR(40) NOT NULL, location VARCHAR(60),
    capacity INT, occupied INT, food_needed INT, water_needed INT);

CREATE TABLE IF NOT EXISTS displaced_people (
    person_id INT PRIMARY KEY, name VARCHAR(60), age INT, gender VARCHAR(10), camp_id INT NOT NULL,
    FOREIGN KEY (camp_id) REFERENCES relief_camps(camp_id));

CREATE TABLE IF NOT EXISTS rescue_operations (
    operation_id INT PRIMARY KEY, incident_id INT NOT NULL, status VARCHAR(15),
    start_time VARCHAR(40), end_time VARCHAR(40),
    FOREIGN KEY (incident_id) REFERENCES incidents(incident_id));

CREATE TABLE IF NOT EXISTS operation_teams (
    operation_id INT, team_id INT, PRIMARY KEY (operation_id, team_id),
    FOREIGN KEY (operation_id) REFERENCES rescue_operations(operation_id),
    FOREIGN KEY (team_id) REFERENCES rescue_teams(team_id));

CREATE TABLE IF NOT EXISTS resource_allocations (
    allocation_id INT PRIMARY KEY, resource_id INT NOT NULL, target_type VARCHAR(10),
    target_id INT, quantity INT, allocated_at VARCHAR(40),
    FOREIGN KEY (resource_id) REFERENCES resources(resource_id));
