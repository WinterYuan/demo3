package com.winter.demo3.controller;

import com.winter.demo3.async.EventModel;
import com.winter.demo3.async.EventProducer;
import com.winter.demo3.async.EventType;
import com.winter.demo3.model.*;
import com.winter.demo3.service.*;
import com.winter.demo3.util.DemoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class QuestionController {
    private static final Logger logger = LoggerFactory.getLogger(QuestionController.class);
    @Autowired
    QuestionService questionService;
    @Autowired
    HostHolder hostHolder;

    @Autowired
    UserService userService;
    @Autowired
    CommentService commentService;

    @Autowired
    LikeService likeService;
    @Autowired
    FollowService followService;
    @Autowired
    EventProducer eventProducer;

    @RequestMapping(value="/question/{qid}")
    public String questionDetail(Model model,@PathVariable("qid") int qid){
        Question question = questionService.selectById(qid);
        model.addAttribute("question",question);
        model.addAttribute("user",userService.getUser(question.getUserId()));

        List<Comment> commentList = commentService.getCommentByEntity(qid, EntityType.ENTITY_QUESTION);
        List<ViewObject> comments = new ArrayList<ViewObject>();
        for(Comment comment : commentList){
            ViewObject vo = new ViewObject();
            vo.set("comment",comment);
            if(hostHolder.getUser()==null){
                vo.set("liked",0);
            }else{
                vo.set("liked",likeService.getLikeStatus(hostHolder.getUser().getId(),EntityType.ENTITY_COMMENT,comment.getId()));
            }
            vo.set("likeCount",likeService.getLikeCount(EntityType.ENTITY_COMMENT,comment.getId()));
            vo.set("user",userService.getUser(comment.getUserId()));
            comments.add(vo);
        }
        model.addAttribute("comments",comments);

        List<ViewObject> followUsers = new ArrayList<ViewObject>();
        List<Integer> users = followService.getFollowers(EntityType.ENTITY_QUESTION,qid,20);
        for(Integer userId : users){
            ViewObject vo = new ViewObject();
            User u = userService.getUser(userId);
            if(u == null){
                continue;
            }
            vo.set("name",u.getName());
            vo.set("headUrl",u.getHeadUrl());
            vo.set("id",u.getId());
            followUsers.add(vo);
        }
        model.addAttribute("followUsers",followUsers);
        if(hostHolder.getUser() != null){
            model.addAttribute("followed",followService.isFollower(hostHolder.getUser().getId(),EntityType.ENTITY_QUESTION,qid));
        }else{
            model.addAttribute("followed",false);
        }
        return "detail";
    }

    @RequestMapping(value="/question/add",method = {RequestMethod.POST})
    @ResponseBody
    public String addQuestion(@RequestParam("title") String title,@RequestParam("content") String content){
        try{
            Question question = new Question();
            question.setContent(content);
            question.setTitle(title);
            question.setCreatedDate(new Date());
            question.setCommentCount(0);
            if(hostHolder.getUser() == null){
                question.setUserId(DemoUtil.ANONYMOUS_USERID);
            }else{
                question.setUserId(hostHolder.getUser().getId());
            }
            if(questionService.addQuestion(question) > 0){
                eventProducer.fireEvent(new EventModel(EventType.ADD_QUESTION)
                        .setActorId(question.getUserId()).setEntityId(question.getId())
                        .setExts("title",question.getTitle()).setExts("content",question.getContent()));
                return DemoUtil.getJSONString(0);
            }
        }catch(Exception e){
            logger.error("增加题目失败"+e.getMessage());
        }
        return DemoUtil.getJSONString(1,"失败");
    }


}
