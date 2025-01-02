val settings = object : TxniTemplateSettings {

	// -------------------- Dependencies ---------------------- //
	override val depsHandler: DependencyHandler get() = object : DependencyHandler {
		override fun addGlobal(deps: DependencyHandlerScope) {
			deps.modImplementation("toni.txnilib:${mod.loader}-${mod.mcVersion}:1.0.20")
			deps.modImplementation("toni.sodiumdynamiclights:${mod.loader}-${mod.mcVersion}:1.0.8") { isTransitive = false }

			deps.runtimeOnly("org.anarres:jcpp:1.4.14") // required for iris
			deps.runtimeOnly("org.antlr:antlr4-runtime:4.13.1") // required for iris
			deps.runtimeOnly("io.github.douira:glsl-transformer:2.0.1") // required for iris
		}

		override fun addFabric(deps: DependencyHandlerScope) {
			if (mod.mcVersion == "1.21.1")
			{
				deps.modImplementation("io.wispforest:accessories-fabric:1.0.0-beta.35+1.21")
				deps.modImplementation(modrinth("iris", "1.8.0-beta.4+1.21-fabric"))
			}
			else {
				deps.modImplementation("io.wispforest:accessories-fabric:1.0.0-beta.38+1.20.1")
				deps.modImplementation(modrinth("iris", "1.7.5+1.20.1"))
			}
		}

		override fun addForge(deps: DependencyHandlerScope) {
			deps.modImplementation("io.wispforest:accessories-neoforge:1.0.0-beta.38+1.20.1") { isTransitive = false }
			deps.minecraftRuntimeLibraries("io.wispforest:endec:0.1.8")
			deps.minecraftRuntimeLibraries("io.wispforest.endec:gson:0.1.5")
			deps.minecraftRuntimeLibraries("io.wispforest.endec:netty:0.1.4")

			deps.modImplementation(modrinth("cloth-config", "11.1.136+forge"))
			deps.modRuntimeOnly("dev.su5ed.sinytra.fabric-api:fabric-api:0.92.2+1.11.8+1.20.1")

			deps.compileOnly(deps.annotationProcessor("io.github.llamalad7:mixinextras-common:0.3.5")!!)
			deps.include(deps.implementation("io.github.llamalad7:mixinextras-forge:0.3.5")!!)

			deps.modImplementation(modrinth("embeddium", "0.3.31+mc1.20.1"))
			deps.modImplementation(modrinth("oculus", "1.20.1-1.7.0"))
			deps.minecraftRuntimeLibraries("org.anarres:jcpp:1.4.14") { isTransitive = false } // required for iris

			deps.modCompileOnly(modrinth("curios", "5.11.0+1.20.1"))
		}

		override fun addNeo(deps: DependencyHandlerScope) {
			deps.modImplementation("io.wispforest:accessories-neoforge:1.0.0-beta.35+1.21")
			deps.minecraftRuntimeLibraries("io.wispforest:endec:0.1.8")
			deps.minecraftRuntimeLibraries("io.wispforest.endec:gson:0.1.5")
			deps.minecraftRuntimeLibraries("io.wispforest.endec:netty:0.1.4")

			deps.modCompileOnly(modrinth("iris", "1.8.0-beta.4+1.21-neoforge"))

			deps.modCompileOnly("curse.maven:adorned-1036809:5740650")
		}
	}


	// ---------- Curseforge/Modrinth Configuration ----------- //
	// For configuring the dependecies that will show up on your mod page.
	override val publishHandler: PublishDependencyHandler get() = object : PublishDependencyHandler {
		override fun addShared(deps: DependencyContainer) {
			deps.requires("txnilib")
			deps.requires("accessories")

			if (mod.isFabric) {
				deps.requires("fabric-api")
			}
		}

		override fun addCurseForge(deps: DependencyContainer) {
			deps.requires("dynamiclights-reforged")
		}

		override fun addModrinth(deps: DependencyContainer) {
			deps.requires("sodium-dynamic-lights")
		}
	}
}


// ---------------TxniTemplate Build Script---------------- //
//   (only edit below this if you know what you're doing)
// -------------------------------------------------------- //

plugins {
	`maven-publish`
	txnitemplate
	application
	kotlin("jvm")
	kotlin("plugin.serialization")
	id("dev.kikugie.j52j") version "1.0"
	id("dev.architectury.loom")
	id("me.modmuss50.mod-publish-plugin")
	id("systems.manifold.manifold-gradle-plugin")
}

// The manifold Gradle plugin version. Update this if you update your IntelliJ Plugin!
manifold { manifoldVersion = "2024.1.34" }

txnitemplate {
	sc = stonecutter
	init()
}

val mod = txnitemplate.mod



// Dependencies
repositories {
	exclusiveMaven("https://www.cursemaven.com", "curse.maven")
	exclusiveMaven("https://api.modrinth.com/maven", "maven.modrinth")
	exclusiveMaven("https://thedarkcolour.github.io/KotlinForForge/", "thedarkcolour")
	maven("https://maven.kikugie.dev/releases")
	maven("https://jitpack.io")
	maven("https://maven.neoforged.net/releases/")
	maven("https://maven.terraformersmc.com/releases/")
	maven("https://raw.githubusercontent.com/Fuzss/modresources/main/maven/")
	maven("https://maven.parchmentmc.org")
	maven("https://maven.txni.dev/releases")
	maven("https://maven.su5ed.dev/releases")
	maven("https://maven.wispforest.io/releases")
	maven("https://maven.fabricmc.net")
	maven("https://maven.shedaniel.me/")
}

dependencies {
	// apply the Manifold processor, do not remove this unless you want to swap back to Stonecutter preprocessor
	implementation(annotationProcessor("systems.manifold:manifold-preprocessor:${manifold.manifoldVersion.get()}")!!)

	compileOnly("org.projectlombok:lombok:1.18.34")
	annotationProcessor("org.projectlombok:lombok:1.18.34")

	@Suppress("UnstableApiUsage")
	mappings(loom.layered {
		officialMojangMappings()
		val parchmentVersion = when (mod.mcVersion) {
			"1.18.2" -> "1.18.2:2022.11.06"
			"1.19.2" -> "1.19.2:2022.11.27"
			"1.20.1" -> "1.20.1:2023.09.03"
			"1.21.1" -> "1.21:2024.07.28"
			else -> ""
		}
		parchment("org.parchmentmc.data:parchment-$parchmentVersion@zip")
	})

	settings.depsHandler.addGlobal(this)

	if (mod.isFabric) {
		modImplementation(settings.depsHandler.modrinth("modmenu", property("deps.modmenu")))

		settings.depsHandler.addFabric(this)
		modImplementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fapi")}")
		modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")

		if (setting("runtime.sodium"))
			modRuntimeOnly(settings.depsHandler.modrinth("sodium", when (mod.mcVersion) {
				"1.21.1" -> "mc1.21-0.6.0-beta.2-fabric"
				"1.20.1" -> "mc1.20.1-0.5.11"
				else -> null
			}))
	}

	if (mod.isForge) {
		settings.depsHandler.addForge(this)
		"forge"("net.minecraftforge:forge:${mod.mcVersion}-${property("deps.fml")}")
	}

	if (mod.isNeo) {
		settings.depsHandler.addNeo(this)
		"neoForge"("net.neoforged:neoforge:${property("deps.fml")}")

		if (setting("runtime.sodium"))
			runtimeOnly(settings.depsHandler.modrinth("sodium", "mc1.21-0.6.0-beta.2-neoforge"))
	}

	vineflowerDecompilerClasspath("org.vineflower:vineflower:1.10.1")
}

fun setting(prop : String) : Boolean = property(prop) == "true"

// Loom config
loom {
	val awFile = rootProject.file("src/main/resources/${mod.id}.accesswidener")
	if (awFile.exists())
		accessWidenerPath.set(awFile)

	if (mod.loader == "forge") forge {
		convertAccessWideners.set(true)
		mixinConfigs("mixins.${mod.id}.json")
	}

	if (mod.isActive) {
		runConfigs.all {
			ideConfigGenerated(true)
			vmArgs("-Dmixin.debug.export=true", "-Dsodium.checks.issue2561=false")
			// Mom look I'm in the codebase!
			//programArgs("--username=${mod.clientuser}", "--uuid=${mod.clientuuid}")
			runDir = "../../run/${stonecutter.current.project}/"
		}
	}

	decompilers {
		get("vineflower").apply {
			options.put("mark-corresponding-synthetics", "1")
		}
	}

	runs {
		register("datagen") {
			client()
			name("DataGen Client")
			vmArg("-Dfabric-api.datagen")
			vmArg("-Dfabric-api.datagen.output-dir=" + getRootDir().toPath().resolve("src/main/generated"))
			vmArg("-Dfabric-api.datagen.modid=${mod.id}")
			ideConfigGenerated(false)
			runDir("build/datagen")
		}
	}
}

sourceSets {
	main {
		resources {
			srcDir("src/main/generated")
			exclude(".cache/")
		}
	}
}

// Tasks
tasks {
	remapJar {
		if (mod.isNeo) {
			atAccessWideners.add("${mod.id}.accesswidener")
		}
	}
}

tasks.withType<Tar>() {
	duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

tasks.withType<Zip>() {
	duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

tasks.compileJava {
	options.encoding = "UTF-8"
	options.compilerArgs.add("-Xplugin:Manifold")
	// modify the JavaCompile task and inject our auto-generated Manifold symbols
	if(!this.name.startsWith("_")) { // check the name, so we don't inject into Forge internal compilation
		ManifoldMC.setupPreprocessor(options.compilerArgs, mod.loader, projectDir, mod.mcVersion, stonecutter.active.project == stonecutter.current.project, false)
	}
}

project.tasks.register("setupManifoldPreprocessors") {
	group = "build"
	ManifoldMC.setupPreprocessor(ArrayList(), mod.loader, projectDir, mod.mcVersion, stonecutter.active.project == stonecutter.current.project, true)
}

tasks.setupChiseledBuild { finalizedBy("setupManifoldPreprocessors") }


val buildAndCollect = tasks.register<Copy>("buildAndCollect") {
	group = "build"
	from(tasks.remapJar.get().archiveFile)
	into(rootProject.layout.buildDirectory.file("libs/${mod.version}"))
	dependsOn("build")
}

if (stonecutter.current.isActive) {
	rootProject.tasks.register("buildActive") {
		group = "project"
		dependsOn(buildAndCollect)
	}

	rootProject.tasks.register("runActive") {
		group = "project"
		dependsOn(tasks.named("runClient"))
	}
}

stonecutter {
	val j21 = eval(mod.mcVersion, ">=1.20.6")
	java {
		withSourcesJar()
		sourceCompatibility = if (j21) JavaVersion.VERSION_21 else JavaVersion.VERSION_17
		targetCompatibility = if (j21) JavaVersion.VERSION_21 else JavaVersion.VERSION_17
	}

	kotlin {
		jvmToolchain(if (j21) 21 else 17)
	}
}

tasks.processResources {
	val map = mapOf(
		"modversion" to mod.version,
		"mc" to mod.mcDep,
		"id" to mod.id,
		"group" to mod.group,
		"author" to mod.author,
		"namespace" to mod.namespace,
		"description" to mod.description,
		"discord" to mod.discord,
		"name" to mod.name,
		"license" to mod.license,
		"github" to mod.github,
		"display_name" to mod.displayName,
		"fml" to if (mod.loader == "neoforge") "1" else "45",
		"mnd" to if (mod.loader == "neoforge") "" else "mandatory = true"
	)

	filesMatching("fabric.mod.json") { expand(map) }
	filesMatching("META-INF/mods.toml") { expand(map) }
	filesMatching("META-INF/neoforge.mods.toml") { expand(map) }
}

// Publishing
publishMods {
	file = tasks.remapJar.get().archiveFile
	additionalFiles.from(tasks.remapSourcesJar.get().archiveFile)
	displayName = "${mod.name} ${mod.loader.replaceFirstChar { it.uppercase() }} ${mod.version} for ${property("mod.mc_title")}"
	version = mod.version
	changelog = rootProject.file("CHANGELOG.md").readText()
	type = STABLE
	modLoaders.add(mod.loader)

	val targets = property("mod.mc_targets").toString().split(' ')

	dryRun = providers.environmentVariable("MODRINTH_TOKEN").getOrNull() == null ||
			providers.environmentVariable("CURSEFORGE_TOKEN").getOrNull() == null

	modrinth {
		projectId = property("publish.modrinth").toString()
		accessToken = providers.environmentVariable("MODRINTH_TOKEN")
		targets.forEach(minecraftVersions::add)
		val deps = DependencyContainer(null, this)
 		settings.publishHandler.addModrinth(deps)
		settings.publishHandler.addShared(deps)
	}

	curseforge {
		projectId = property("publish.curseforge").toString()
		accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
		targets.forEach(minecraftVersions::add)
		val deps = DependencyContainer(this, null)
		settings.publishHandler.addCurseForge(deps)
		settings.publishHandler.addShared(deps)
	}
}

publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			groupId = "${property("mod.group")}.${mod.id}"
			version = mod.version
			artifactId = "${mod.loader}-${mod.mcVersion}" //base.archivesName.get()

			from(components["java"])
		}
	}

	repositories {
		val username = "MAVEN_USERNAME".let { System.getenv(it) ?: findProperty(it) }?.toString()
		val password = "MAVEN_PASSWORD".let { System.getenv(it) ?: findProperty(it) }?.toString()

		if (username == null || password == null) {
			println("No maven credentials found.")
            return@repositories;
		}

		val mavenURI = if (properties["publish.use_snapshot_maven"] == "true") "snapshots" else "releases"
		maven {
			name = "${mod.author}_$mavenURI"
			url = uri("https://${property("publish.maven_url").toString()}/$mavenURI")
			credentials {
				this.username = System.getenv("MAVEN_USERNAME")
				this.password = System.getenv("MAVEN_PASSWORD")
			}
		}
	}
}