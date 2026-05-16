-- FitLife Gym Management — create database
-- Run as MySQL root (or user with CREATE DATABASE privilege)

CREATE DATABASE IF NOT EXISTS gym_management
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE gym_management;

SELECT 'Database gym_management is ready.' AS status;
