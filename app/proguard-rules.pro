# 崩溃堆栈可读:保留行号与源文件名(R8 混淆后仍能定位)
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# 常规注解与反射安全网(Compose/Kotlin 自带 consumer 规则之外的兜底)
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
