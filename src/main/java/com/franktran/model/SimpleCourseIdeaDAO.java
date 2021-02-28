package com.franktran.model;

import java.util.ArrayList;
import java.util.List;

public class SimpleCourseIdeaDAO implements CourseIdeaDAO {

  private List<CourseIdea> courseIdeas;

  public SimpleCourseIdeaDAO() {
    this.courseIdeas = new ArrayList<>();
  }

  @Override
  public boolean add(CourseIdea courseIdea) {
    return courseIdeas.add(courseIdea);
  }

  @Override
  public List<CourseIdea> findAll() {
    return new ArrayList<>(courseIdeas);
  }
}
