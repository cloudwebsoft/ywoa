package com.cloudweb.oa.api;

import cn.js.fan.util.ErrMsgException;
import com.redmoon.oa.base.IFormDAO;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.flow.WorkflowActionDb;
import com.redmoon.oa.flow.WorkflowDb;
import com.redmoon.oa.visual.FormDAO;
import com.redmoon.oa.visual.ModulePrivDb;
import com.redmoon.oa.visual.ModuleSetupDb;

import javax.servlet.http.HttpServletRequest;
import java.util.Vector;

public interface IModuleRender {

    String rendForAdd(ModuleSetupDb msd, FormDb fd, String content, Vector<FormField> fields);

    String rend(ModuleSetupDb msd, FormDAO fdao, String formElementId, String content, Vector<FormField> fields, Vector<FormField> vdisable);

    String report(FormDAO fdao, String content, boolean isNest);

    String reportForArchive(IFormDAO fdao, String content);

    String[] rendForNestTable(FormDb fd, String content, Vector<FormField> fields, String FORM_FLEMENT_ID, boolean isAdd, IFormDAO fdao);

    String getContentMacroReplaced(ModuleSetupDb msd, FormDAO fdao, String content, Vector<FormField> fields, Vector<FormField> vdisable);

    String rendNestSheetCtlRelated(FormDAO fdao, FormDb fd, WorkflowActionDb wfa, Vector<FormField> vNestFormField) throws ErrMsgException;
}
