package com.godroup.plugins.movetogroup;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import static java.util.Arrays.asList;
import java.util.Collection;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import static java.util.stream.Collectors.toList;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.project.ui.ProjectGroup;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;
import org.openide.windows.TopComponent;

@ActionID(
        category = "MoveToGroupActions",
        id = "com.godroup.plugins.movetogroup.MoveToGroupAction"
)
@ActionRegistration(
        displayName = "#CTL_MoveToGroupAction",
        lazy = false
)
@ActionReference(path = "Projects/Actions")
@Messages("CTL_MoveToGroupAction=Move To Group")
public final class MoveToGroupAction extends AbstractAction implements ActionListener, Presenter.Popup {

    private static final long serialVersionUID = -2716693145500890430L;

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

        groups.stream().filter(t -> !t[1].equals(activeGroupName)).forEach(t -> {
            JMenuItem item = new JMenuItem(t[1]); // groupName as menu text.
            item.setActionCommand(t[0]); // groupId as action command.
            item.addActionListener(this);

            main.add(item);
        });

        return main;
    }

    /**
     * 获取当前项目组
     *
     * @return
     */
    private String getActiveProjectGroup() {
        ProjectGroup activeProjectGroup = OpenProjects.getDefault().getActiveProjectGroup();
        if (activeProjectGroup == null) {
            return null;
        }

        return activeProjectGroup.getName();
    }

    /**
     * 获取当前待定的项目
     *
     * @return
     */
    private List<ProjectInformation> getCurrentProjectList() {
        Collection<? extends Project> currentProjects = TopComponent.getRegistry().getActivated().getLookup().lookupAll(Project.class);
        if (currentProjects == null) {
            return null;
        }

        return currentProjects.stream().map(t -> ProjectUtils.getInformation(t)).collect(toList());
    }

    /**
     * 将当前项目移至指定的项目组
     *
     * @param groupId
     * @param projectInfo
     * @return
     */
    private boolean moveToGroup(String groupId, ProjectInformation projectInfo) {
        String preferPath = "org/netbeans/modules/projectui/groups/" + groupId;

        Preferences groupNode = getPreferences(preferPath);
        if (null != groupNode) {
            // Append new project path
            String oldPath = groupNode.get("path", null);
            String newPath = Utilities.toURI(new File(projectInfo.getProject().getProjectDirectory().getPath())).toString();
            if (oldPath != null) {
                newPath = oldPath + " " + newPath;
            }

            groupNode.put("path", newPath.replace("null ", ""));

            // Close current project.
            OpenProjects.getDefault().close(new Project[]{projectInfo.getProject()});
            return true;
        }

        return false;
    }

    /**
     * 获取所有项目组
     *
     * @return
     */
    private List<String[]> getProjectGroups() {
        Preferences groupNode = getPreferences("org/netbeans/modules/projectui/groups");
        if (groupNode != null) {
            try {
                List<String> childrenNames = asList(groupNode.childrenNames());
                return childrenNames.stream().map(t -> {
                    String groupId = t;
                    String groupName = groupId;

                    Preferences childGroupNode = getPreferences("org/netbeans/modules/projectui/groups/" + groupId);
                    if (childGroupNode != null) {
                        groupName = childGroupNode.get("name", null);
                    }

                    return new String[] {groupId, groupName};
                }).collect(toList());
            } catch (BackingStoreException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        return new ArrayList<>(0);
    }

    /**
     * Get the preference for the given node path.
     *
     * @param path configuration path like "org/netbeans/modules/projectui"
     * @return {@link Preferences} or null
     */
    private Preferences getPreferences(String path) {
        try {
            if (NbPreferences.root().nodeExists(path)) {
                return NbPreferences.root().node(path);
            }
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
        }

        return null;
    }
}
