package com.interpreters.merlin;

import java.util.List;


public abstract class Stmt {


	public interface Visitor<R> {
		R visitExpressionStmt(ExpressionStmt stmt);
		R visitBlockStmt(BlockStmt stmt);
		R visitIFStmt(IFStmt stmt);
		R visitWHILEStmt(WHILEStmt stmt);
		R visitFORStmt(FORStmt stmt);
		R visitRETURNStmt(RETURNStmt stmt);
		R visitVarDeclStmt(VarDeclStmt stmt);
		R visitFunDeclStmt(FunDeclStmt stmt);
	}


	public static class ExpressionStmt extends Stmt{
		public ExpressionStmt(Expr expression) {
			this.expression = expression;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitExpressionStmt(this);
		}
		public final Expr expression;
	}



	public static class BlockStmt extends Stmt{
		public BlockStmt(List<Stmt> statements) {
			this.statements = statements;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitBlockStmt(this);
		}
		public final List<Stmt> statements;
	}



	public static class IFStmt extends Stmt{
		public IFStmt(Expr condition, Stmt thenBranch, Stmt elseBranch) {
			this.condition = condition;
			this.thenBranch = thenBranch;
			this.elseBranch = elseBranch;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitIFStmt(this);
		}
		public final Expr condition;
		public final  Stmt thenBranch;
		public final  Stmt elseBranch;
	}



	public static class WHILEStmt extends Stmt{
		public WHILEStmt(Expr condition, Stmt body) {
			this.condition = condition;
			this.body = body;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitWHILEStmt(this);
		}
		public final Expr condition;
		public final  Stmt body;
	}



	public static class FORStmt extends Stmt{
		public FORStmt(Stmt initializer, Expr condition, Expr increment, Stmt body) {
			this.initializer = initializer;
			this.condition = condition;
			this.increment = increment;
			this.body = body;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitFORStmt(this);
		}
		public final Stmt initializer;
		public final  Expr condition;
		public final  Expr increment;
		public final  Stmt body;
	}



	public static class RETURNStmt extends Stmt{
		public RETURNStmt(Token keyword, Expr value) {
			this.keyword = keyword;
			this.value = value;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitRETURNStmt(this);
		}
		public final Token keyword;
		public final  Expr value;
	}



	public static class VarDeclStmt extends Stmt{
		public VarDeclStmt(List<Token> names, List<Expr> initializers) {
			this.names = names;
			this.initializers = initializers;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitVarDeclStmt(this);
		}
		public final List<Token> names;
		public final  List<Expr> initializers;
	}



	public static class FunDeclStmt extends Stmt{
		public FunDeclStmt(Token name, Expr.FunctionExpr description) {
			this.name = name;
			this.description = description;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitFunDeclStmt(this);
		}
		public final Token name;
		public final  Expr.FunctionExpr description;
	}




	public abstract <R> R accept(Visitor<R> visitor);

}
