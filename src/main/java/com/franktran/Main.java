package com.franktran;

import com.franktran.model.CourseIdea;
import com.franktran.model.CourseIdeaDAO;
import com.franktran.model.NotFoundException;
import com.franktran.model.SimpleCourseIdeaDAO;
import spark.ModelAndView;
import spark.Request;
import spark.template.handlebars.HandlebarsTemplateEngine;

import java.util.HashMap;
import java.util.Map;

import static spark.Spark.*;

public class Main {

  private static final String FLASH_MESSAGE_KEY = "flash_message";

  public static void main(String[] args) {
    staticFileLocation("/public");

    CourseIdeaDAO dao = new SimpleCourseIdeaDAO();

    before((req, res) -> {
      if (req.cookie("username") != null) {
        req.attribute("username", req.cookie("username"));
      }
    });

    get("/", (req, res) -> {
      Map<String, String> model = new HashMap<>();
      model.put("username", req.attribute("username"));
      model.put("flashMessage", captureFlashMessage(req));
      return new ModelAndView(model, "index.hbs");
    }, new HandlebarsTemplateEngine());

    post("/sign-in", (req, res) -> {
      Map<String, String> model = new HashMap<>();
      String username = req.queryParams("username");
      res.cookie("username", username);
      model.put("username", username);
      return new ModelAndView(model, "index.hbs");
    }, new HandlebarsTemplateEngine());

    before("/ideas", (req, res) -> {
      if (req.attribute("username") == null) {
        setFlashMessage(req, "Oops, Please sign in first!");
        res.redirect("/");
        halt();
      }
    });

    get("/ideas", (req, res) -> {
      Map<String, Object> model = new HashMap<>();
      model.put("ideas", dao.findAll());
      model.put("flashMessage", captureFlashMessage(req));
      return new ModelAndView(model, "ideas.hbs");
    }, new HandlebarsTemplateEngine());

    post("/ideas", (req, res) -> {
      String title = req.queryParams("title");
      CourseIdea idea = new CourseIdea(title, req.attribute("username"));
      dao.add(idea);
      res.redirect("/ideas");
      return null;
    });

    get("/ideas/:slug", (req, res) -> {
      Map<String, Object> model = new HashMap<>();
      model.put("idea", dao.findBySlug(req.params("slug")));
      return new ModelAndView(model, "idea.hbs");
    }, new HandlebarsTemplateEngine());

    post("/ideas/:slug/vote", (req, res) -> {
      CourseIdea idea = dao.findBySlug(req.params("slug"));
      boolean added = idea.addVoter(req.attribute("username"));
      if (added) {
        setFlashMessage(req, "Thanks for your vote!");
      } else {
        setFlashMessage(req, "You already voted!");
      }
      res.redirect("/ideas");
      return null;
    });

    exception(NotFoundException.class, (ex, req, res) -> {
      res.status(404);
      HandlebarsTemplateEngine engine = new HandlebarsTemplateEngine();
      String html = engine.render(new ModelAndView(null, "not-found.hbs"));
      res.body(html);
    });
  }

  private static void setFlashMessage(Request req, String message) {
    req.session().attribute(FLASH_MESSAGE_KEY, message);
  }

  private static String getFlashMessage(Request req) {
    if (req.session(false) == null) {
      return null;
    }
    if (!req.session().attributes().contains(FLASH_MESSAGE_KEY)) {
      return null;
    }
    return req.session().attribute(FLASH_MESSAGE_KEY);
  }

  private static String captureFlashMessage(Request req) {
    String message = getFlashMessage(req);
    if (message != null) {
      req.session().removeAttribute(FLASH_MESSAGE_KEY);
    }
    return message;
  }
}
