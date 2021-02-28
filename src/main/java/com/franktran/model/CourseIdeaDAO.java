package com.franktran.model;

import java.util.List;

public interface CourseIdeaDAO {

  boolean add(CourseIdea courseIdea);

  List<CourseIdea> findAll();
}
