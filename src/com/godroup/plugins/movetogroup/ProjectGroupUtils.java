package com.godroup.plugins.movetogroup;

import java.io.File;
import java.util.ArrayList;
import static java.util.Arrays.asList;
import java.util.Collection;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import static java.util.stream.Collectors.toList;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.project.ui.ProjectGroup;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

public final class ProjectGroupUtils {

    /**
     * Get active project group name.
     *
     * @return
     */
    public static String getActiveProjectGroup() {
        ProjectGroup activeProjectGroup = OpenProjects.getDefault().getActiveProjectGroup();
        if (activeProjectGroup == null) {
            return null;
        }

        return activeProjectGroup.getName();
    }

    /**
     * Get selected project information list.
     *
     * @return
     */
    public static List<ProjectInformation> getCurrentProjectList() {
        Collection<? extends Project> currentProjects = TopComponent.getRegistry().getActivated().getLookup().lookupAll(Project.class);
        if (currentProjects == null) {
            return null;
        }

        return currentProjects.stream().map(t -> ProjectUtils.getInformation(t)).collect(toList());
    }

    /**
     * Add project path to group, and close selected project(s).
     *
     * @param groupId
     * @param projectInfo
     * @return
     */
    public static boolean moveToGroup(String groupId, ProjectInformation projectInfo) {
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
     * Get all project groups.
     *
     * @return String[0]=groupId, String[1]=groupName
     */
    public static List<String[]> getProjectGroups() {
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
    private static Preferences getPreferences(String path) {
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
