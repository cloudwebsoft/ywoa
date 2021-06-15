package com.redmoon.oa.meeting;

import cn.js.fan.util.ErrMsgException;
import javax.servlet.http.HttpServletRequest;

public class BoardroomMgr {
    public BoardroomMgr() {
    }

    public BoardroomDb getBoardroomDb(int id) {
        BoardroomDb bd = new BoardroomDb();
        return bd.getBoardroomDb(id);
    }

    public synchronized boolean create(HttpServletRequest request) throws
            ErrMsgException {
        BoardroomForm fc = new BoardroomForm();
        fc.checkCreate(request);

        BoardroomDb ft = fc.getBoardroomDb();
        return ft.create();
    }

    public synchronized boolean del(HttpServletRequest request) throws
            ErrMsgException {
        BoardroomForm fc = new BoardroomForm();
        fc.checkDel(request);

        BoardroomDb bd = fc.getBoardroomDb();
        bd = bd.getBoardroomDb(bd.getId());
        return bd.del();
    }

    public synchronized boolean modify(HttpServletRequest request) throws
            ErrMsgException {
        BoardroomForm fc = new BoardroomForm();
        fc.checkModify(request);

        BoardroomDb ft = fc.getBoardroomDb();
        BoardroomDb ftd = ft.getBoardroomDb(ft.getId());
        ftd.setName(ft.getName());
        ftd.setDescription(ft.getDescription());
        ftd.setPersonNum(ft.getPersonNum());
        ftd.setEquipment(ft.getEquipment());
        ftd.setAddress(ft.getAddress());
        return ftd.save();
    }


}
