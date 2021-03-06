package com.winter.demo3.service;

import com.winter.demo3.dao.CommentDAO;
import com.winter.demo3.model.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class CommentService {
    @Autowired
    CommentDAO commentDAO;

    @Autowired
    SensitiveService sensitiveService;
    public List<Comment> getCommentByEntity(int entityId,int entityType){
        return commentDAO.selectCommentByEntity(entityId,entityType);
    }

    public int addComment(Comment comment){
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveService.filter(comment.getContent()));
        return commentDAO.addComment(comment) > 0 ? comment.getId() : 0;
    }

    public int getCommentCount(int entityId,int entityType){
        return commentDAO.getCommentCount(entityId,entityType);
    }
    public int getUserCommentCount(int userId) {
        return commentDAO.getUserCommentCount(userId);
    }
    public void deleteComment(int entityId,int entityType){
        commentDAO.updateStatus(entityId,entityType,1);
    }

    public Comment getCommentById(int id){
        return commentDAO.getCommentById(id);
    }
}
