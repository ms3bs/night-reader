plugins { kotlin("jvm") }
dependencies {
    implementation(project(":core"))
    implementation(project(":db:core"))
    implementation("org.mindrot:jbcrypt:0.4")
}