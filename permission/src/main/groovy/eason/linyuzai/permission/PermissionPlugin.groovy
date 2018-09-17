package eason.linyuzai.permission

import com.android.build.api.transform.*
import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.pipeline.TransformManager
import eason.linyuzai.permissions.EasonPermissions
import javassist.ClassPool
import javassist.CtClass
import javassist.CtField
import javassist.CtMethod
import javassist.CtNewMethod
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils

class PermissionPlugin extends Transform implements Plugin<Project> {
    private static ClassPool pool = ClassPool.getDefault()

    private Project project

    @Override
    void apply(Project project) {
        this.project = project
        System.out.println("========================")
        Properties properties = new Properties()
        // 使用InPutStream流读取properties文件
        BufferedReader bufferedReader = new BufferedReader(new FileReader(project.getRootDir().absolutePath + "/local.properties"))
        properties.load(bufferedReader)
        // 获取key对应的value值
        String sdk = properties.getProperty("sdk.dir")
        System.out.println(sdk + "/platforms")
        File file = new File(sdk + "/platforms")
        String[] fs = file.list()
        if (fs.length > 0) {
            pool.appendClassPath(sdk + "/platforms/" + fs[0] + "/android.jar")
            def android = project.extensions.getByType(AppExtension)
            android.registerTransform(this)
        }
        System.out.println("========================")
    }
    // 设置我们自定义的Transform对应的Task名称
    // 类似：TransformClassesWithPreDexForXXX
    @Override
    String getName() {
        return "PermissionAnnotation"
    }

    // 指定输入的类型，通过这里的设定，可以指定我们要处理的文件类型
    //这样确保其他类型的文件不会传入
    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    // 指定Transform的作用范围
    @Override
    Set<QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    //具体的处理
    @Override
    void transform(Context context, Collection<TransformInput> inputs,
                   Collection<TransformInput> referencedInputs,
                   TransformOutputProvider outputProvider, boolean isIncremental)
            throws IOException, TransformException, InterruptedException {
        inputs.each { TransformInput input ->
            //对类型为jar文件的input进行遍历
            input.jarInputs.each { JarInput jarInput ->

                //jar文件一般是第三方依赖库jar文件

                // 重命名输出文件（同目录copyFile会冲突）
                def jarName = jarInput.name
                if (jarName.contains("eason.linyuzai:eason-permissions") ||
                        jarName.contains("com.android.support:support-fragment"))
                    pool.appendClassPath(jarInput.file.absolutePath)
                System.out.println("path:" + jarInput.file.absolutePath + "," + jarInput.name)
                System.out.println("========================")
                def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
                if (jarName.endsWith(".jar")) {
                    jarName = jarName.substring(0, jarName.length() - 4)
                }
                //生成输出路径
                def dest = outputProvider.getContentLocation(jarName + md5Name,
                        jarInput.contentTypes, jarInput.scopes, Format.JAR)
                //将输入内容复制到输出
                FileUtils.copyFile(jarInput.file, dest)
            }
            //对类型为“文件夹”的input进行遍历
            input.directoryInputs.each { DirectoryInput directoryInput ->
                //PermissionInject.inject(project.)
                //文件夹里面包含的是我们手写的类以及R.class、BuildConfig.class以及R$XXX.class等
                inject(directoryInput.file.absolutePath)
                // 获取output目录
                def dest = outputProvider.getContentLocation(directoryInput.name,
                        directoryInput.contentTypes, directoryInput.scopes,
                        Format.DIRECTORY)
                // 将input的目录复制到output指定目录
                FileUtils.copyDirectory(directoryInput.file, dest)
            }
        }
    }

    static void inject(String path) {
        pool.appendClassPath(path)
        File dir = new File(path)
        if (dir.isDirectory()) {
            dir.eachFileRecurse { File file ->
                String filePath = file.absolutePath
                //确保当前文件是class文件，并且不是系统自动生成的class文件
                boolean hasAdd = false
                long timestamp = System.currentTimeMillis()
                if (filePath.endsWith(".class")
                        && !filePath.contains('R$')
                        && !filePath.contains('R.class')
                        && !filePath.contains("BuildConfig.class")) {
                    int end = filePath.length() - 6 // .class = 6
                    int index = path.length() + 1
                    String className = filePath.substring(index, end)
                            .replace('\\', '.').replace('/', '.')
                    System.out.println("className:" + className)
                    System.out.println("========================")
                    //开始修改class文件
                    CtClass ctClass = pool.getCtClass(className)
                    if (ctClass.isFrozen()) {
                        ctClass.defrost()
                    }

                    //System.out.println("========================")
                    //System.out.println("CtClass:" + ctClass.getSuperclass())
                    CtMethod[] ctMethods = ctClass.getDeclaredMethods()
                    ctMethods.each { CtMethod ctMethod ->
                        EasonPermissions p = ctMethod.getAnnotation(EasonPermissions)
                        if (p != null) {
                            if (!hasAdd) {
                                ctClass.addField(CtField.make("private eason.linyuzai.permissions._Permission _permission" + timestamp + "=null;", ctClass))
                                ctClass.addField(CtField.make("private boolean _needRequest" + timestamp + " = true;", ctClass))
                                ctClass.addMethod(CtMethod.make("public void onFinalResult(boolean grant) {" +
                                        "        if (grant) {" +
                                        "            _needRequest" + timestamp + " = false;" +
                                        "            " + ctMethod.getName() + "();" +
                                        "        } else {" +
                                        "            _needRequest" + timestamp + " = true;" +
                                        "        }" +
                                        "    }", ctClass))
                                ctClass.addMethod(CtMethod.make("public void onPermissionsResult(String[] permissions, boolean[] grants) {}", ctClass))
                                ctClass.addInterface(pool.get("eason.linyuzai.permissions._Permission\$Callback"))
                                hasAdd = true
                            }
                            StringBuilder builder = new StringBuilder()
                            for (String v : p.value()) {
                                builder.append(",").append(v)
                            }
                            builder.deleteCharAt(0)
                            //System.out.println("ctMethod:" + )
                            //CtNewMethod.copy()
                            //CtMethod newMethod = new CtMethod(ctMethod, ctClass, null)
                            //newMethod.setName(ctMethod.getName() + "WithPlugin")
                            /*ctMethod.insertBefore("if (_permission == null)\n" +
                                    "            _permission = new eason.linyuzai.permissions._Permission(" + className + ".this);\n" +
                                    "        _permission.requestPermissions(new String[]{" + builder.toString() + "}, new eason.linyuzai.permissions._Permission.Callback() {\n" +
                                    "\n" +
                                    "            @Override\n" +
                                    "            public void onFinalResult(boolean grant) {\n" +
                                    "                if (grant) {")
                            ctMethod.insertAfter("}\n" +
                                    "            }\n" +
                                    "\n" +
                                    "            @Override\n" +
                                    "            public void onPermissionsResult(String[] permissions, boolean[] grants) {\n" +
                                    "\n" +
                                    "            }\n" +
                                    "        });")
                            ctMethod*/
                            /*ctMethod.insertBefore("if (_needRequest" + timestamp + ") {\n" +
                                    "            if (_permission" + timestamp + " == null)\n" +
                                    "                _permission" + timestamp + " = new eason.linyuzai.permissions._Permission(" + className + ".this);\n" +
                                    "            _permission" + timestamp + ".requestPermissions(new String[]{" + builder.toString() + "}, new eason.linyuzai.permissions._Permission.Callback() {\n" +
                                    "                public void onFinalResult(boolean grant) {\n" +
                                    "                    if (grant) {\n" +
                                    "                        needRequest" + timestamp + " = false;\n" +
                                    "                        " + "aa" + "();\n" +
                                    "                    } else {\n" +
                                    "                        needRequest" + timestamp + " = true;\n" +
                                    "                    }\n" +
                                    "                }\n" +
                                    "\n" +
                                    "                public void onPermissionsResult(String[] permissions, boolean[] grants) {\n" +
                                    "\n" +
                                    "                }\n" +
                                    "            });\n" +
                                    "            return;\n" +
                                    "        }")*/
                            String before = "if (_needRequest" + timestamp + ") {" +
                                    "            if (_permission" + timestamp + " == null) {" +
                                    "                _permission" + timestamp + " = eason.linyuzai.permissions._Permission.get(\$0);}" +
                                    "            _permission" + timestamp + ".requestPermissions(eason.linyuzai.permissions._Permission.getPermissionArray(\"" + builder.toString() + "\"),\$0);" +
                                    "            return;" +
                                    "        }"

                            System.out.println("========================")
                            System.out.println(before)
                            ctMethod.insertBefore(before)
                            ctMethod.insertAfter("_needRequest" + timestamp + " = false;")
                        }
                        //ctMethod.instrument()
                        //ctMethod.insertAt()
                        //ctMethod.insertAfter()
                        //ctMethod.getMethodInfo()
                        //System.out.println("========================")
                        //System.out.println("EBody:" + ctMethod.body)
                        /*CtMethod newMethod = new CtMethod(ctMethod, ctClass, null)
                        newMethod.setName(ctMethod.getName() + "Plugin")
                        ctClass.addMethod(newMethod)*/
                    }

                    ctClass.writeFile(path)
                    ctClass.detach()
                }
            }
        }
    }
}