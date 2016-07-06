package com.godroup.plugins.movetogroup;

import static com.godroup.plugins.movetogroup.ProjectGroupUtils.getActiveProjectGroup;
import static com.godroup.plugins.movetogroup.ProjectGroupUtils.getCurrentProjectList;
import static com.godroup.plugins.movetogroup.ProjectGroupUtils.getProjectGroups;
import static com.godroup.plugins.movetogroup.ProjectGroupUtils.moveToGroup;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.netbeans.api.project.ProjectInformation;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.NbBundle.Messages;
import org.openide.util.actions.Presenter;

@ActionID(
        category = "File",
        id = "com.godroup.plugins.movetogroup.MoveToGroupAction"
)
@ActionRegistration(
        displayName = "#CTL_MoveToGroupAction",
        lazy = false
)
@ActionReferences({
    @ActionReference(path = "Menu/File", position = 1350),
    @ActionReference(path = "Projects/Actions")
})
@Messages("CTL_MoveToGroupAction=Move To Group")
public final class MoveToGroupAction extends AbstractAction implements ActionListener, Presenter.Popup, DynamicMenuContent {

    private static final long serialVersionUID = -2716693145500890430L;

    @Override
    public JComponent[] getMenuPresenters() {

        return new JComponent[]{getPopupPresenter()};
    }

    @Override
    public JComponent[] synchMenuPresenters(JComponent[] items) {
        return getMenuPresenters();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        List<ProjectInformation> projectInfos = getCurrentProjectList();
        if (projectInfos != null) {
            projectInfos.forEach(info -> moveToGroup(e.getActionCommand(), info));
        }
    }

    @Override
    public JMenuItem getPopupPresenter() {
        JMenu main = new JMenu(Bundle.CTL_MoveToGroupAction());

        List<String[]> groups = getProjectGroups();
        String activeGroupName = getActiveProjectGroup();

        if (activeGroupName != null) {
            groups.add(0, new String[] {"none_group", "(none)"});
        }

        groups.stream().filter(t -> !t[1].equals(activeGroupName)).forEach(t -> {
            JMenuItem item = new JMenuItem(t[1]); // groupName as menu text.
            item.setActionCommand(t[0]); // groupId as action command.
            item.addActionListener(this);

            main.add(item);
        });

        return main;
    }
}
