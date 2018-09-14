package eason.linyuzai.permission

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class PermissionPlugin implements Plugin<Project> {

    void apply(Project project) {
        System.out.println("========================")
        System.out.println("hello gradle plugin!")
        def android = project.extensions.getByType(AppExtension)
        android.registerTransform(new PermissionTransform(project))
        System.out.println("========================")
    }
}