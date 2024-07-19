package tv.iptv.tun.tviptv.processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import tv.iptv.tun.tviptv.annotations.SqlDelightEntity
import kotlin.math.log

class KSPSqlDelightProcessor(
    private val codeBaseGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(SqlDelightEntity::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()

        symbols.forEach { processClass(it) }
        return emptyList()
    }

    private fun processClass(ksClassDeclaration: KSClassDeclaration) {
        val packageName = ksClassDeclaration.packageName.asString()
        val className = ksClassDeclaration.simpleName.asString()
        val annotation =
            ksClassDeclaration.annotations.first { it.shortName.asString() == "SqlDelightEntity" }
        val tableName = annotation.arguments.first().value as String
        val columns = ksClassDeclaration.getAllProperties().mapNotNull { property ->
            val columnAnnotation =
                property.annotations.firstOrNull { it.shortName.asString() == "SqlDelightColumn" }
            val isPrimaryKeys =
                property.annotations.findLast { it.shortName.asString() == "SqlDelightPrimaryKey" } != null
            columnAnnotation?.let {
                val columnName = it.arguments.first().value as String
                val primaryKey = if (isPrimaryKeys) " PRIMARY KEY" else ""
                "$columnName ${getSqlType(property.type.resolve().declaration.simpleName.asString())}$primaryKey"
            }
        }
        generateSqlFile(packageName, className, tableName, columns.toList())
    }

    private fun generateSqlFile(
        packageName: String,
        className: String,
        tableName: String,
        columns: List<String>
    ) {
        val fileName = "${tableName}.sq"
        val fileSpec = """
            CREATE TABLE $tableName (
                ${columns.joinToString(",\n    ")}
            );
            
            insert${className}:
            INSERT INTO $tableName (${columns.joinToString(", ") { it.split(" ")[0] }}) VALUES (${columns.joinToString(", ") { "?" }});
            
            selectAll${className}s:
            SELECT * FROM $tableName;
        """.trimIndent()

        val file = codeBaseGenerator.createNewFile(
            Dependencies.ALL_FILES,
            packageName.replace('.', '/'),
            fileName
        )
        logger.info("File: $file")
        file.write(fileSpec.toByteArray())
        file.close()
    }

    private fun getSqlType(kotlinType: String): String {
        return when (kotlinType) {
            "Int" -> "INTEGER"
            "String" -> "TEXT"
            else -> "TEXT"
        }
    }
}

class SqlDelightProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return KSPSqlDelightProcessor(environment.codeGenerator, environment.logger)
    }
}