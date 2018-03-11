/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.wso2.ballerinalang.compiler.semantics.analyzer;

import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.statements.StatementNode;
import org.ballerinalang.model.tree.types.BuiltInReferenceTypeNode;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationAttributeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BServiceSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BStructSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BBuiltInRefType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangAction;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachmentPoint;
import org.wso2.ballerinalang.compiler.tree.BLangConnector;
import org.wso2.ballerinalang.compiler.tree.BLangDocumentation;
import org.wso2.ballerinalang.compiler.tree.BLangEndpoint;
import org.wso2.ballerinalang.compiler.tree.BLangEnum;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangInvokableNode;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangStruct;
import org.wso2.ballerinalang.compiler.tree.BLangTransformer;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangWorker;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangGroupBy;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangDocumentationAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangVariableReference;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAbort;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBind;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBreak;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangLock;
import org.wso2.ballerinalang.compiler.tree.statements.BLangNext;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangThrow;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTryCatchFinally;
import org.wso2.ballerinalang.compiler.tree.statements.BLangVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerReceive;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerSend;
import org.wso2.ballerinalang.compiler.tree.statements.BLangXMLNSStatement;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLog;
import org.wso2.ballerinalang.util.Flags;
import org.wso2.ballerinalang.util.Lists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @since 0.94
 */
public class SemanticAnalyzer extends BLangNodeVisitor {

    private static final CompilerContext.Key<SemanticAnalyzer> SYMBOL_ANALYZER_KEY =
            new CompilerContext.Key<>();

    private SymbolTable symTable;
    private SymbolEnter symbolEnter;
    private Names names;
    private SymbolResolver symResolver;
    private TypeChecker typeChecker;
    private Types types;
    private EndpointSPIAnalyzer endpointSPIAnalyzer;
    private BLangDiagnosticLog dlog;

    private SymbolEnv env;
    private BType expType;
    private DiagnosticCode diagCode;
    private BType resType;

    public static SemanticAnalyzer getInstance(CompilerContext context) {
        SemanticAnalyzer semAnalyzer = context.get(SYMBOL_ANALYZER_KEY);
        if (semAnalyzer == null) {
            semAnalyzer = new SemanticAnalyzer(context);
        }

        return semAnalyzer;
    }

    public SemanticAnalyzer(CompilerContext context) {
        context.put(SYMBOL_ANALYZER_KEY, this);

        this.symTable = SymbolTable.getInstance(context);
        this.symbolEnter = SymbolEnter.getInstance(context);
        this.names = Names.getInstance(context);
        this.symResolver = SymbolResolver.getInstance(context);
        this.typeChecker = TypeChecker.getInstance(context);
        this.types = Types.getInstance(context);
        this.endpointSPIAnalyzer = EndpointSPIAnalyzer.getInstance(context);
        this.dlog = BLangDiagnosticLog.getInstance(context);
    }

    public BLangPackage analyze(BLangPackage pkgNode) {
        pkgNode.accept(this);
        return pkgNode;
    }


    // Visitor methods

    public void visit(BLangPackage pkgNode) {
        if (pkgNode.completedPhases.contains(CompilerPhase.TYPE_CHECK)) {
            return;
        }
        SymbolEnv pkgEnv = this.symTable.pkgEnvMap.get(pkgNode.symbol);

        // Visit all the imported packages
        pkgNode.imports.forEach(importNode -> analyzeDef(importNode, pkgEnv));

        // Then visit each top-level element sorted using the compilation unit
        pkgNode.topLevelNodes.forEach(topLevelNode -> analyzeDef((BLangNode) topLevelNode, pkgEnv));

        analyzeDef(pkgNode.initFunction, pkgEnv);
        analyzeDef(pkgNode.startFunction, pkgEnv);
        analyzeDef(pkgNode.stopFunction, pkgEnv);

        pkgNode.completedPhases.add(CompilerPhase.TYPE_CHECK);
    }

    public void visit(BLangImportPackage importPkgNode) {
        BPackageSymbol pkgSymbol = importPkgNode.symbol;
        SymbolEnv pkgEnv = this.symTable.pkgEnvMap.get(pkgSymbol);
        if (pkgEnv == null) {
            return;
        }

        analyzeDef(pkgEnv.node, pkgEnv);
    }

    public void visit(BLangXMLNS xmlnsNode) {
        xmlnsNode.type = symTable.stringType;

        // Namespace node already having the symbol means we are inside an init-function,
        // and the symbol has already been declared by the original statement.
        if (xmlnsNode.symbol != null) {
            return;
        }

        symbolEnter.defineNode(xmlnsNode, env);
        typeChecker.checkExpr(xmlnsNode.namespaceURI, env, Lists.of(symTable.stringType));
    }

    public void visit(BLangXMLNSStatement xmlnsStmtNode) {
        analyzeNode(xmlnsStmtNode.xmlnsDecl, env);
    }

    public void visit(BLangFunction funcNode) {
        SymbolEnv funcEnv = SymbolEnv.createFunctionEnv(funcNode, funcNode.symbol.scope, env);
        funcNode.annAttachments.forEach(annotationAttachment -> {
            annotationAttachment.attachmentPoint =
                    new BLangAnnotationAttachmentPoint(BLangAnnotationAttachmentPoint.AttachmentPoint.FUNCTION);
            this.analyzeDef(annotationAttachment, funcEnv);
        });
        funcNode.docAttachments.forEach(doc -> analyzeDef(doc, funcEnv));

        // Check for native functions
        if (Symbols.isNative(funcNode.symbol)) {
            return;
        }

        funcNode.endpoints.forEach(e -> analyzeDef(e, env));
        analyzeStmt(funcNode.body, funcEnv);

        this.processWorkers(funcNode, funcEnv);
    }

    private void processWorkers(BLangInvokableNode invNode, SymbolEnv invEnv) {
        if (invNode.workers.size() > 0) {
            invEnv.scope.entries.putAll(invNode.body.scope.entries);
            invNode.workers.forEach(e -> this.symbolEnter.defineNode(e, invEnv));
            invNode.workers.forEach(e -> analyzeNode(e, invEnv));
        }
    }

    public void visit(BLangStruct structNode) {
        BSymbol structSymbol = structNode.symbol;
        SymbolEnv structEnv = SymbolEnv.createPkgLevelSymbolEnv(structNode, structSymbol.scope, env);
        structNode.fields.forEach(field -> analyzeDef(field, structEnv));

        structNode.annAttachments.forEach(annotationAttachment -> {
            annotationAttachment.attachmentPoint =
                    new BLangAnnotationAttachmentPoint(BLangAnnotationAttachmentPoint.AttachmentPoint.STRUCT);
            annotationAttachment.accept(this);
        });
        structNode.docAttachments.forEach(doc -> analyzeDef(doc, structEnv));
    }

    @Override
    public void visit(BLangEnum enumNode) {
        BSymbol enumSymbol = enumNode.symbol;
        SymbolEnv enumEnv = SymbolEnv.createPkgLevelSymbolEnv(enumNode, enumSymbol.scope, env);

        enumNode.annAttachments.forEach(annotationAttachment -> {
            annotationAttachment.attachmentPoint = new BLangAnnotationAttachmentPoint(
                    BLangAnnotationAttachmentPoint.AttachmentPoint.ENUM);
            annotationAttachment.accept(this);
        });
        enumNode.docAttachments.forEach(doc -> analyzeDef(doc, enumEnv));
    }

    @Override
    public void visit(BLangDocumentation docNode) {
        Set<BLangIdentifier> visitedAttributes = new HashSet<>();
        for (BLangDocumentationAttribute attribute : docNode.attributes) {
            if (!visitedAttributes.add(attribute.documentationField)) {
                this.dlog.warning(attribute.pos, DiagnosticCode.DUPLICATE_DOCUMENTED_ATTRIBUTE,
                        attribute.documentationField);
                continue;
            }
            Name attributeName = names.fromIdNode(attribute.documentationField);
            BSymbol attributeSymbol = this.env.scope.lookup(attributeName).symbol;
            if (attributeSymbol == null) {
                this.dlog.warning(attribute.pos, DiagnosticCode.NO_SUCH_DOCUMENTABLE_ATTRIBUTE,
                        attribute.documentationField, attribute.docTag.getValue());
                continue;
            }
            int ownerSymTag = env.scope.owner.tag;
            if ((ownerSymTag & SymTag.ANNOTATION) == SymTag.ANNOTATION) {
                if (attributeSymbol.tag != SymTag.ANNOTATION_ATTRIBUTE
                        || ((BAnnotationAttributeSymbol) attributeSymbol).docTag != attribute.docTag) {
                    this.dlog.warning(attribute.pos, DiagnosticCode.NO_SUCH_DOCUMENTABLE_ATTRIBUTE,
                            attribute.documentationField, attribute.docTag.getValue());
                    continue;
                }
            } else {
                if (attributeSymbol.tag != SymTag.VARIABLE
                        || ((BVarSymbol) attributeSymbol).docTag != attribute.docTag) {
                    this.dlog.warning(attribute.pos, DiagnosticCode.NO_SUCH_DOCUMENTABLE_ATTRIBUTE,
                            attribute.documentationField, attribute.docTag.getValue());
                    continue;
                }
            }
            attribute.type = attributeSymbol.type;
        }
    }

    public void visit(BLangAnnotation annotationNode) {
        SymbolEnv annotationEnv = SymbolEnv.createAnnotationEnv(annotationNode, annotationNode.symbol.scope, env);
        annotationNode.annAttachments.forEach(annotationAttachment -> {
            annotationAttachment.attachmentPoint =
                    new BLangAnnotationAttachmentPoint(BLangAnnotationAttachmentPoint.AttachmentPoint.ANNOTATION);
            annotationAttachment.accept(this);
        });
        annotationNode.docAttachments.forEach(doc -> analyzeDef(doc, annotationEnv));
    }

    public void visit(BLangAnnotationAttachment annAttachmentNode) {
        BSymbol symbol = this.symResolver.resolveAnnotation(annAttachmentNode.pos, env,
                names.fromString(annAttachmentNode.pkgAlias.getValue()),
                names.fromString(annAttachmentNode.getAnnotationName().getValue()));
        if (symbol == this.symTable.notFoundSymbol) {
            this.dlog.error(annAttachmentNode.pos, DiagnosticCode.UNDEFINED_ANNOTATION,
                    annAttachmentNode.getAnnotationName().getValue());
            return;
        }
        // Validate Attachment Point against the Annotation Definition.
        BAnnotationSymbol annotationSymbol = (BAnnotationSymbol) symbol;
        annAttachmentNode.annotationSymbol = annotationSymbol;
        if (annotationSymbol.getAttachmentPoints() != null && annotationSymbol.getAttachmentPoints().size() > 0) {
            BLangAnnotationAttachmentPoint[] attachmentPointsArrray =
                    new BLangAnnotationAttachmentPoint[annotationSymbol.getAttachmentPoints().size()];
            Optional<BLangAnnotationAttachmentPoint> matchingAttachmentPoint = Arrays
                    .stream(annotationSymbol.getAttachmentPoints().toArray(attachmentPointsArrray))
                    .filter(attachmentPoint -> attachmentPoint.equals(annAttachmentNode.attachmentPoint))
                    .findAny();
            if (!matchingAttachmentPoint.isPresent()) {
                String msg = annAttachmentNode.attachmentPoint.getAttachmentPoint().getValue();
                this.dlog.error(annAttachmentNode.pos, DiagnosticCode.ANNOTATION_NOT_ALLOWED,
                        annotationSymbol, msg);
            }
        }
        // Validate Annotation Attachment data struct against Annotation Definition struct.
        validateAnnotationAttachmentExpr(annAttachmentNode, annotationSymbol);
    }

    private void validateAnnotationAttachmentExpr(BLangAnnotationAttachment annAttachmentNode, BAnnotationSymbol
            annotationSymbol) {
        if (annotationSymbol.attachedType == null) {
            if (annAttachmentNode.expr != null) {
                this.dlog.error(annAttachmentNode.pos, DiagnosticCode.ANNOTATION_ATTACHMENT_NO_VALUE,
                        annotationSymbol.name);
            }
            return;
        }
        if (annAttachmentNode.expr != null) {
            this.typeChecker.checkExpr(annAttachmentNode.expr, env, Lists.of(annotationSymbol.attachedType.type));
        }
    }

    public void visit(BLangVariable varNode) {
        int ownerSymTag = env.scope.owner.tag;
        if ((ownerSymTag & SymTag.INVOKABLE) == SymTag.INVOKABLE) {
            // This is a variable declared in a function, an action or a resource
            // If the variable is parameter then the variable symbol is already defined
            if (varNode.symbol == null) {
                symbolEnter.defineNode(varNode, env);
            }
        }

        // Here we validate annotation attachments for package level variables.
        varNode.annAttachments.forEach(a -> {
            a.attachmentPoint =
                    new BLangAnnotationAttachmentPoint(BLangAnnotationAttachmentPoint.AttachmentPoint.CONST);
            a.accept(this);
        });
        // Here we validate document attachments for package level variables.
        varNode.docAttachments.forEach(doc -> {
            doc.accept(this);
        });

        // Analyze the init expression
        if (varNode.expr != null) {
            // Here we create a new symbol environment to catch self references by keep the current
            // variable symbol in the symbol environment
            // e.g. int a = x + a;
            SymbolEnv varInitEnv = SymbolEnv.createVarInitEnv(varNode, env, varNode.symbol);

            // If the variable is a package/service/connector level variable, we don't need to check types.
            // It will we done during the init-function of the respective construct is visited.
            if ((ownerSymTag & SymTag.PACKAGE) != SymTag.PACKAGE &&
                    (ownerSymTag & SymTag.SERVICE) != SymTag.SERVICE &&
                    (ownerSymTag & SymTag.CONNECTOR) != SymTag.CONNECTOR) {
                typeChecker.checkExpr(varNode.expr, varInitEnv, Lists.of(varNode.symbol.type));
            }

        }
        varNode.type = varNode.symbol.type;
    }


    // Statements

    public void visit(BLangBlockStmt blockNode) {
        SymbolEnv blockEnv = SymbolEnv.createBlockEnv(blockNode, env);
        blockNode.stmts.forEach(stmt -> analyzeStmt(stmt, blockEnv));
    }

    public void visit(BLangVariableDef varDefNode) {
        analyzeDef(varDefNode.var, env);
    }

    public void visit(BLangAssignment assignNode) {
        if (assignNode.isDeclaredWithVar()) {
            handleAssignNodeWithVar(assignNode);
            return;
        }

        // Check each LHS expression.
        List<BType> expTypes = new ArrayList<>();
        for (BLangExpression expr : assignNode.varRefs) {
            // In assignment, lhs supports only simpleVarRef, indexBasedAccess, filedBasedAccess expressions.
            if (expr.getKind() != NodeKind.SIMPLE_VARIABLE_REF &&
                    expr.getKind() != NodeKind.INDEX_BASED_ACCESS_EXPR &&
                    expr.getKind() != NodeKind.FIELD_BASED_ACCESS_EXPR &&
                    expr.getKind() != NodeKind.XML_ATTRIBUTE_ACCESS_EXPR) {
                dlog.error(expr.pos, DiagnosticCode.INVALID_VARIABLE_ASSIGNMENT, expr);
                expTypes.add(symTable.errType);
                continue;
            }

            // Evaluate the variable reference expression.
            BLangVariableReference varRef = (BLangVariableReference) expr;
            varRef.lhsVar = true;
            typeChecker.checkExpr(varRef, env).get(0);

            // Check whether we've got an enumerator access expression here.
            if (varRef.getKind() == NodeKind.FIELD_BASED_ACCESS_EXPR &&
                    ((BLangFieldBasedAccess) varRef).expr.type.tag == TypeTags.ENUM) {
                dlog.error(varRef.pos, DiagnosticCode.INVALID_VARIABLE_ASSIGNMENT, varRef);
                expTypes.add(symTable.errType);
                continue;
            }

            expTypes.add(varRef.type);
            checkConstantAssignment(varRef);
        }

        typeChecker.checkExpr(assignNode.expr, this.env, expTypes);
    }

    public void visit(BLangBind bindNode) {
        List<BType> expTypes = new ArrayList<>();
        // Check each LHS expression.
        BLangExpression varRef = bindNode.varRef;
        ((BLangVariableReference) varRef).lhsVar = true;
        expTypes.add(typeChecker.checkExpr(varRef, env).get(0));
        checkConstantAssignment(varRef);
        typeChecker.checkExpr(bindNode.expr, this.env, expTypes);
    }

    private void checkConstantAssignment(BLangExpression varRef) {
        if (varRef.type == symTable.errType) {
            return;
        }

        if (varRef.getKind() != NodeKind.SIMPLE_VARIABLE_REF) {
            return;
        }

        BLangSimpleVarRef simpleVarRef = (BLangSimpleVarRef) varRef;
        if (simpleVarRef.pkgSymbol != null && simpleVarRef.pkgSymbol.tag == SymTag.XMLNS) {
            dlog.error(varRef.pos, DiagnosticCode.XML_QNAME_UPDATE_NOT_ALLOWED);
            return;
        }

        Name varName = names.fromIdNode(simpleVarRef.variableName);
        if (!Names.IGNORE.equals(varName) && simpleVarRef.symbol.flags == Flags.CONST
                && env.enclInvokable != env.enclPkg.initFunction) {
            dlog.error(varRef.pos, DiagnosticCode.CANNOT_ASSIGN_VALUE_CONSTANT, varRef);
        }
    }

    public void visit(BLangExpressionStmt exprStmtNode) {
        // Creates a new environment here.
        SymbolEnv stmtEnv = new SymbolEnv(exprStmtNode, this.env.scope);
        this.env.copyTo(stmtEnv);
        List<BType> bTypes = typeChecker.checkExpr(exprStmtNode.expr, stmtEnv, new ArrayList<>());
        if (bTypes.size() > 0 && !(bTypes.size() == 1 && bTypes.get(0) == symTable.errType)) {
            dlog.error(exprStmtNode.pos, DiagnosticCode.ASSIGNMENT_REQUIRED);
        }
    }

    public void visit(BLangIf ifNode) {
        typeChecker.checkExpr(ifNode.expr, env, Lists.of(symTable.booleanType));
        analyzeStmt(ifNode.body, env);

        if (ifNode.elseStmt != null) {
            analyzeStmt(ifNode.elseStmt, env);
        }
    }

    public void visit(BLangForeach foreach) {
        typeChecker.checkExpr(foreach.collection, env);
        foreach.varTypes = types.checkForeachTypes(foreach.collection, foreach.varRefs.size());
        SymbolEnv blockEnv = SymbolEnv.createBlockEnv(foreach.body, env);
        handleForeachVariables(foreach, foreach.varTypes, blockEnv);
        analyzeStmt(foreach.body, blockEnv);
    }

    public void visit(BLangWhile whileNode) {
        typeChecker.checkExpr(whileNode.expr, env, Lists.of(symTable.booleanType));
        analyzeStmt(whileNode.body, env);
    }

    @Override
    public void visit(BLangLock lockNode) {
        analyzeStmt(lockNode.body, env);
    }

    public void visit(BLangConnector connectorNode) {
        BSymbol connectorSymbol = connectorNode.symbol;
        SymbolEnv connectorEnv = SymbolEnv.createConnectorEnv(connectorNode, connectorSymbol.scope, env);
        connectorNode.annAttachments.forEach(a -> {
            a.attachmentPoint =
                    new BLangAnnotationAttachmentPoint(BLangAnnotationAttachmentPoint.AttachmentPoint.CONNECTOR);
            this.analyzeDef(a, connectorEnv);
        });
        connectorNode.docAttachments.forEach(doc -> analyzeDef(doc, connectorEnv));

        connectorNode.params.forEach(param -> this.analyzeDef(param, connectorEnv));
        connectorNode.varDefs.forEach(varDef -> this.analyzeDef(varDef, connectorEnv));
        connectorNode.endpoints.forEach(e -> analyzeDef(e, env));
        this.analyzeDef(connectorNode.initFunction, connectorEnv);
        connectorNode.actions.forEach(action -> this.analyzeDef(action, connectorEnv));
        this.analyzeDef(connectorNode.initAction, connectorEnv);
    }

    public void visit(BLangAction actionNode) {
        BSymbol actionSymbol = actionNode.symbol;

        SymbolEnv actionEnv = SymbolEnv.createResourceActionSymbolEnv(actionNode, actionSymbol.scope, env);
        actionNode.annAttachments.forEach(a -> {
            a.attachmentPoint =
                    new BLangAnnotationAttachmentPoint(BLangAnnotationAttachmentPoint.AttachmentPoint.ACTION);
            this.analyzeDef(a, actionEnv);
        });
        actionNode.docAttachments.forEach(doc -> analyzeDef(doc, actionEnv));

        if (Symbols.isNative(actionSymbol)) {
            return;
        }

        actionNode.params.forEach(p -> this.analyzeDef(p, actionEnv));
        actionNode.endpoints.forEach(e -> analyzeDef(e, env));
        analyzeStmt(actionNode.body, actionEnv);
        this.processWorkers(actionNode, actionEnv);
    }

    public void visit(BLangService serviceNode) {
        BServiceSymbol serviceSymbol = (BServiceSymbol) serviceNode.symbol;
        SymbolEnv serviceEnv = SymbolEnv.createPkgLevelSymbolEnv(serviceNode, serviceSymbol.scope, env);
        serviceSymbol.endpointType = symResolver.resolveTypeNode(serviceNode.endpointType, env);
        endpointSPIAnalyzer.isValidEndpointType(serviceNode.endpointType.pos, serviceSymbol.endpointType);
        serviceNode.annAttachments.forEach(a -> {
            a.attachmentPoint =
                    new BLangAnnotationAttachmentPoint(BLangAnnotationAttachmentPoint.AttachmentPoint.SERVICE);
            this.analyzeDef(a, serviceEnv);
        });
        serviceNode.docAttachments.forEach(doc -> analyzeDef(doc, serviceEnv));
        serviceNode.vars.forEach(v -> this.analyzeDef(v, serviceEnv));
        serviceNode.endpoints.forEach(e -> this.analyzeDef(e, serviceEnv));
        this.analyzeDef(serviceNode.initFunction, serviceEnv);
        serviceNode.resources.forEach(r -> this.analyzeDef(r, serviceEnv));
    }

    public void visit(BLangResource resourceNode) {
        BSymbol resourceSymbol = resourceNode.symbol;
        SymbolEnv resourceEnv = SymbolEnv.createResourceActionSymbolEnv(resourceNode, resourceSymbol.scope, env);
        resourceNode.annAttachments.forEach(a -> {
            a.attachmentPoint =
                    new BLangAnnotationAttachmentPoint(BLangAnnotationAttachmentPoint.AttachmentPoint.RESOURCE);
            this.analyzeDef(a, resourceEnv);
        });
        resourceNode.docAttachments.forEach(doc -> analyzeDef(doc, resourceEnv));

        resourceNode.params.forEach(p -> this.analyzeDef(p, resourceEnv));
        resourceNode.endpoints.forEach(e -> analyzeDef(e, env));
        analyzeStmt(resourceNode.body, resourceEnv);
        this.processWorkers(resourceNode, resourceEnv);
    }

    public void visit(BLangTryCatchFinally tryCatchFinally) {
        analyzeStmt(tryCatchFinally.tryBody, env);
        tryCatchFinally.catchBlocks.forEach(c -> analyzeNode(c, env));
        if (tryCatchFinally.finallyBody != null) {
            analyzeStmt(tryCatchFinally.finallyBody, env);
        }
    }

    public void visit(BLangCatch bLangCatch) {
        SymbolEnv catchBlockEnv = SymbolEnv.createBlockEnv(bLangCatch.body, env);
        analyzeNode(bLangCatch.param, catchBlockEnv);
        if (!this.types.checkStructEquivalency(bLangCatch.param.type, symTable.errStructType)) {
            dlog.error(bLangCatch.param.pos, DiagnosticCode.INCOMPATIBLE_TYPES, symTable.errStructType,
                    bLangCatch.param.type);
        }
        analyzeStmt(bLangCatch.body, catchBlockEnv);
    }

    @Override
    public void visit(BLangTransaction transactionNode) {
        analyzeStmt(transactionNode.transactionBody, env);
        if (transactionNode.failedBody != null) {
            analyzeStmt(transactionNode.failedBody, env);
        }
        if (transactionNode.retryCount != null) {
            typeChecker.checkExpr(transactionNode.retryCount, env, Lists.of(symTable.intType));
            checkRetryStmtValidity(transactionNode.retryCount);
        }
    }

    @Override
    public void visit(BLangAbort abortNode) {
    }

    private boolean isJoinResultType(BLangVariable var) {
        BLangType type = var.typeNode;
        if (type instanceof BuiltInReferenceTypeNode) {
            return ((BuiltInReferenceTypeNode) type).getTypeKind() == TypeKind.MAP;
        }
        return false;
    }

    private BLangVariableDef createVarDef(BLangVariable var) {
        BLangVariableDef varDefNode = new BLangVariableDef();
        varDefNode.var = var;
        varDefNode.pos = var.pos;
        return varDefNode;
    }

    private BLangBlockStmt generateCodeBlock(StatementNode... statements) {
        BLangBlockStmt block = new BLangBlockStmt();
        for (StatementNode stmt : statements) {
            block.addStatement(stmt);
        }
        return block;
    }

    @Override
    public void visit(BLangForkJoin forkJoin) {
        SymbolEnv forkJoinEnv = SymbolEnv.createFolkJoinEnv(forkJoin, this.env);
        forkJoin.workers.forEach(e -> this.symbolEnter.defineNode(e, forkJoinEnv));
        forkJoin.workers.forEach(e -> this.analyzeDef(e, forkJoinEnv));
        if (!this.isJoinResultType(forkJoin.joinResultVar)) {
            this.dlog.error(forkJoin.joinResultVar.pos, DiagnosticCode.INVALID_WORKER_JOIN_RESULT_TYPE);
        }
        /* create code black and environment for join result section, i.e. (map results) */
        BLangBlockStmt joinResultsBlock = this.generateCodeBlock(this.createVarDef(forkJoin.joinResultVar));
        SymbolEnv joinResultsEnv = SymbolEnv.createBlockEnv(joinResultsBlock, this.env);
        this.analyzeNode(joinResultsBlock, joinResultsEnv);
        /* create an environment for the join body, making the enclosing environment the earlier
         * join result's environment */
        SymbolEnv joinBodyEnv = SymbolEnv.createBlockEnv(forkJoin.joinedBody, joinResultsEnv);
        this.analyzeNode(forkJoin.joinedBody, joinBodyEnv);

        if (forkJoin.timeoutExpression != null) {
            if (!this.isJoinResultType(forkJoin.timeoutVariable)) {
                this.dlog.error(forkJoin.timeoutVariable.pos, DiagnosticCode.INVALID_WORKER_TIMEOUT_RESULT_TYPE);
            }
            /* create code black and environment for timeout section */
            BLangBlockStmt timeoutVarBlock = this.generateCodeBlock(this.createVarDef(forkJoin.timeoutVariable));
            SymbolEnv timeoutVarEnv = SymbolEnv.createBlockEnv(timeoutVarBlock, this.env);
            this.typeChecker.checkExpr(forkJoin.timeoutExpression,
                    timeoutVarEnv, Arrays.asList(symTable.intType));
            this.analyzeNode(timeoutVarBlock, timeoutVarEnv);
            /* create an environment for the timeout body, making the enclosing environment the earlier
             * timeout var's environment */
            SymbolEnv timeoutBodyEnv = SymbolEnv.createBlockEnv(forkJoin.timeoutBody, timeoutVarEnv);
            this.analyzeNode(forkJoin.timeoutBody, timeoutBodyEnv);
        }

        this.validateJoinWorkerList(forkJoin, forkJoinEnv);
    }

    private void validateJoinWorkerList(BLangForkJoin forkJoin, SymbolEnv forkJoinEnv) {
        forkJoin.joinedWorkers.forEach(e -> {
            if (!this.workerExists(forkJoinEnv, e.value)) {
                this.dlog.error(forkJoin.pos, DiagnosticCode.UNDEFINED_WORKER, e.value);
            }
        });
    }

    @Override
    public void visit(BLangWorker workerNode) {
        SymbolEnv workerEnv = SymbolEnv.createWorkerEnv(workerNode, this.env);
        this.analyzeNode(workerNode.body, workerEnv);
    }

    @Override
    public void visit(BLangEndpoint endpointNode) {
        endpointNode.annAttachments.forEach(annotationAttachment -> {
            annotationAttachment.attachmentPoint =
                    new BLangAnnotationAttachmentPoint(BLangAnnotationAttachmentPoint.AttachmentPoint.ENDPOINT);
            this.analyzeDef(annotationAttachment, env);
        });
        BType configType = symTable.errType;
        if (endpointNode.symbol != null && endpointNode.symbol.type.tag == TypeTags.STRUCT) {
            configType = endpointSPIAnalyzer.getEndpointConfigType((BStructSymbol) endpointNode.symbol.type.tsymbol);
        }
        if (endpointNode.configurationExpr != null) {
            this.typeChecker.checkExpr(endpointNode.configurationExpr, env, Lists.of(configType));
        }
    }

    private boolean isInTopLevelWorkerEnv() {
        return this.env.enclEnv.node.getKind() == NodeKind.WORKER;
    }

    private boolean workerExists(SymbolEnv env, String workerName) {
        BSymbol symbol = this.symResolver.lookupSymbol(env, new Name(workerName), SymTag.WORKER);
        return (symbol != this.symTable.notFoundSymbol);
    }

    @Override
    public void visit(BLangWorkerSend workerSendNode) {
        workerSendNode.env = this.env;
        workerSendNode.exprs.forEach(e -> this.typeChecker.checkExpr(e, this.env));
        if (!this.isInTopLevelWorkerEnv()) {
            this.dlog.error(workerSendNode.pos, DiagnosticCode.INVALID_WORKER_SEND_POSITION);
        }
        if (!workerSendNode.isForkJoinSend) {
            String workerName = workerSendNode.workerIdentifier.getValue();
            if (!this.workerExists(this.env, workerName)) {
                this.dlog.error(workerSendNode.pos, DiagnosticCode.UNDEFINED_WORKER, workerName);
            }
        }
    }

    @Override
    public void visit(BLangWorkerReceive workerReceiveNode) {
        workerReceiveNode.exprs.forEach(e -> this.typeChecker.checkExpr(e, this.env));
        if (!this.isInTopLevelWorkerEnv()) {
            this.dlog.error(workerReceiveNode.pos, DiagnosticCode.INVALID_WORKER_RECEIVE_POSITION);
        }
        String workerName = workerReceiveNode.workerIdentifier.getValue();
        if (!this.workerExists(this.env, workerName)) {
            this.dlog.error(workerReceiveNode.pos, DiagnosticCode.UNDEFINED_WORKER, workerName);
        }
    }

    private boolean checkReturnValueCounts(BLangReturn returnNode) {
        boolean success = false;
        int expRetCount = this.env.enclInvokable.getReturnParameters().size();
        int actualRetCount = returnNode.exprs.size();
        if (expRetCount > 1 && actualRetCount <= 1) {
            this.dlog.error(returnNode.pos, DiagnosticCode.MULTI_VALUE_RETURN_EXPECTED);
        } else if (expRetCount == 1 && actualRetCount > 1) {
            this.dlog.error(returnNode.pos, DiagnosticCode.SINGLE_VALUE_RETURN_EXPECTED);
        } else if (expRetCount == 0 && actualRetCount >= 1) {
            this.dlog.error(returnNode.pos, DiagnosticCode.RETURN_VALUE_NOT_EXPECTED);
        } else if (expRetCount > actualRetCount) {
            this.dlog.error(returnNode.pos, DiagnosticCode.NOT_ENOUGH_RETURN_VALUES);
        } else if (expRetCount < actualRetCount) {
            this.dlog.error(returnNode.pos, DiagnosticCode.TOO_MANY_RETURN_VALUES);
        } else {
            success = true;
        }
        return success;
    }

    private boolean isInvocationExpr(BLangExpression expr) {
        return expr.getKind() == NodeKind.INVOCATION;
    }

    @Override
    public void visit(BLangReturn returnNode) {
        if (returnNode.exprs.size() == 1 && this.isInvocationExpr(returnNode.exprs.get(0))) {
            /* a single return expression can be expanded to match a multi-value return */
            this.typeChecker.checkExpr(returnNode.exprs.get(0), this.env,
                    this.env.enclInvokable.getReturnParameters().stream()
                            .map(e -> e.getTypeNode().type)
                            .collect(Collectors.toList()));
        } else {
            if (returnNode.exprs.size() == 0 && this.env.enclInvokable.getReturnParameters().size() > 0
                    && !this.env.enclInvokable.getReturnParameters().get(0).name.value.isEmpty()) {
                // Return stmt has no expressions, but function/action has returns and they are named returns.
                // Rewrite tree at desuger phase.
                returnNode.namedReturnVariables = this.env.enclInvokable.getReturnParameters();
                return;
            }
            if (this.checkReturnValueCounts(returnNode)) {
                for (int i = 0; i < returnNode.exprs.size(); i++) {
                    this.typeChecker.checkExpr(returnNode.exprs.get(i), this.env,
                            Arrays.asList(this.env.enclInvokable.getReturnParameters().get(i).getTypeNode().type));
                }
            }
        }
    }

    BType analyzeDef(BLangNode node, SymbolEnv env) {
        return analyzeNode(node, env);
    }

    BType analyzeStmt(BLangStatement stmtNode, SymbolEnv env) {
        return analyzeNode(stmtNode, env);
    }

    BType analyzeNode(BLangNode node, SymbolEnv env) {
        return analyzeNode(node, env, symTable.noType, null);
    }

    public void visit(BLangNext nextNode) {
        /* ignore */
    }

    public void visit(BLangBreak breakNode) {
        /* ignore */
    }

    @Override
    public void visit(BLangThrow throwNode) {
        this.typeChecker.checkExpr(throwNode.expr, env);
        if (!types.checkStructEquivalency(throwNode.expr.type, symTable.errStructType)) {
            dlog.error(throwNode.expr.pos, DiagnosticCode.INCOMPATIBLE_TYPES, symTable.errStructType,
                    throwNode.expr.type);
        }
    }

    public void visit(BLangGroupBy groupBy) {
        throw  new AssertionError();
    }

    @Override
    public void visit(BLangTransformer transformerNode) {
        SymbolEnv transformerEnv = SymbolEnv.createTransformerEnv(transformerNode, transformerNode.symbol.scope, env);
        transformerNode.annAttachments.forEach(annotationAttachment -> {
            annotationAttachment.attachmentPoint = new BLangAnnotationAttachmentPoint(
                    BLangAnnotationAttachmentPoint.AttachmentPoint.TRANSFORMER);
            this.analyzeDef(annotationAttachment, transformerEnv);
        });
        transformerNode.docAttachments.forEach(doc -> analyzeDef(doc, transformerEnv));

        validateTransformerMappingType(transformerNode.source);
        validateTransformerMappingType(transformerNode.retParams.get(0));

        analyzeStmt(transformerNode.body, transformerEnv);

        // TODO: update this accordingly once the unsafe conversion are supported
        int returnCount = transformerNode.retParams.size();
        if (returnCount == 0) {
            dlog.error(transformerNode.pos, DiagnosticCode.TRANSFORMER_MUST_HAVE_OUTPUT);
        } else if (returnCount > 1) {
            dlog.error(transformerNode.pos, DiagnosticCode.TOO_MANY_OUTPUTS_FOR_TRANSFORMER, 1, returnCount);
        }

        this.processWorkers(transformerNode, transformerEnv);
    }

    BType analyzeNode(BLangNode node, SymbolEnv env, BType expType, DiagnosticCode diagCode) {
        SymbolEnv prevEnv = this.env;
        BType preExpType = this.expType;
        DiagnosticCode preDiagCode = this.diagCode;

        // TODO Check the possibility of using a try/finally here
        this.env = env;
        this.expType = expType;
        this.diagCode = diagCode;
        node.accept(this);
        this.env = prevEnv;
        this.expType = preExpType;
        this.diagCode = preDiagCode;

        return resType;
    }

    // Private methods

    private void handleForeachVariables(BLangForeach foreachStmt, List<BType> varTypes, SymbolEnv env) {
        for (int i = 0; i < foreachStmt.varRefs.size(); i++) {
            BLangExpression varRef = foreachStmt.varRefs.get(i);
            // foreach variables supports only simpleVarRef expressions only.
            if (varRef.getKind() != NodeKind.SIMPLE_VARIABLE_REF) {
                dlog.error(varRef.pos, DiagnosticCode.INVALID_VARIABLE_ASSIGNMENT, varRef);
                continue;
            }
            BLangSimpleVarRef simpleVarRef = (BLangSimpleVarRef) varRef;
            simpleVarRef.lhsVar = true;
            Name varName = names.fromIdNode(simpleVarRef.variableName);
            if (varName == Names.IGNORE) {
                simpleVarRef.type = this.symTable.noType;
                typeChecker.checkExpr(simpleVarRef, env);
                continue;
            }
            // Check variable symbol for existence.
            BSymbol symbol = symResolver.lookupSymbol(env, varName, SymTag.VARIABLE);
            if (symbol == symTable.notFoundSymbol) {
                symbolEnter.defineVarSymbol(simpleVarRef.pos, Collections.emptySet(), varTypes.get(i), varName, env);
                typeChecker.checkExpr(simpleVarRef, env);
            } else {
                dlog.error(simpleVarRef.pos, DiagnosticCode.REDECLARED_SYMBOL, varName);
            }
        }
    }

    private void handleAssignNodeWithVar(BLangAssignment assignNode) {
        int ignoredCount = 0;
        int createdSymbolCount = 0;

        List<Name> newVariables = new ArrayList<Name>();

        List<BType> expTypes = new ArrayList<>();
        // Check each LHS expression.
        for (int i = 0; i < assignNode.varRefs.size(); i++) {
            BLangExpression varRef = assignNode.varRefs.get(i);
            // If the assignment is declared with "var", then lhs supports only simpleVarRef expressions only.
            if (varRef.getKind() != NodeKind.SIMPLE_VARIABLE_REF) {
                dlog.error(varRef.pos, DiagnosticCode.INVALID_VARIABLE_ASSIGNMENT, varRef);
                expTypes.add(symTable.errType);
                continue;
            }
            // Check variable symbol if exists.
            BLangSimpleVarRef simpleVarRef = (BLangSimpleVarRef) varRef;
            ((BLangVariableReference) varRef).lhsVar = true;
            Name varName = names.fromIdNode(simpleVarRef.variableName);
            if (varName == Names.IGNORE) {
                ignoredCount++;
                simpleVarRef.type = this.symTable.noType;
                expTypes.add(symTable.noType);
                typeChecker.checkExpr(simpleVarRef, env);
                continue;
            }

            BSymbol symbol = symResolver.lookupSymbol(env, varName, SymTag.VARIABLE);
            if (symbol == symTable.notFoundSymbol) {
                createdSymbolCount++;
                newVariables.add(varName);
                expTypes.add(symTable.noType);
            } else {
                expTypes.add(symbol.type);
            }
        }

        if (ignoredCount == assignNode.varRefs.size() || createdSymbolCount == 0) {
            dlog.error(assignNode.pos, DiagnosticCode.NO_NEW_VARIABLES_VAR_ASSIGNMENT);
        }
        // Check RHS expressions with expected type list.
        final List<BType> rhsTypes = typeChecker.checkExpr(assignNode.expr, this.env, expTypes);

        // visit all lhs expressions
        for (int i = 0; i < assignNode.varRefs.size(); i++) {
            BLangExpression varRef = assignNode.varRefs.get(i);
            if (varRef.getKind() != NodeKind.SIMPLE_VARIABLE_REF) {
                continue;
            }
            BType actualType = rhsTypes.get(i);
            BLangSimpleVarRef simpleVarRef = (BLangSimpleVarRef) varRef;
            Name varName = names.fromIdNode(simpleVarRef.variableName);
            if (newVariables.contains(varName)) {
                // define new variables
                this.symbolEnter.defineVarSymbol(simpleVarRef.pos, Collections.emptySet(), actualType, varName, env);
            }
            typeChecker.checkExpr(simpleVarRef, env);
        }
    }

    private void checkRetryStmtValidity(BLangExpression retryCountExpr) {
        boolean error = true;
        NodeKind retryKind = retryCountExpr.getKind();
        if (retryKind == NodeKind.LITERAL) {
            if (retryCountExpr.type.tag == TypeTags.INT) {
                int retryCount = Integer.parseInt(((BLangLiteral) retryCountExpr).getValue().toString());
                if (retryCount >= 0) {
                    error = false;
                }
            }
        } else if (retryKind == NodeKind.SIMPLE_VARIABLE_REF) {
            if (((BLangSimpleVarRef) retryCountExpr).symbol.flags == Flags.CONST) {
                if (((BLangSimpleVarRef) retryCountExpr).symbol.type.tag == TypeTags.INT) {
                    error = false;
                }
            }
        }
        if (error) {
            this.dlog.error(retryCountExpr.pos, DiagnosticCode.INVALID_RETRY_COUNT);
        }
    }

    private void validateTransformerMappingType(BLangVariable param) {
        BType type = param.type;
        if (types.isValueType(type) || (type instanceof BBuiltInRefType) || type.tag == TypeTags.STRUCT) {
            return;
        }

        dlog.error(param.pos, DiagnosticCode.TRANSFORMER_UNSUPPORTED_TYPES, type);
    }

}
