package com.interpreters.merlin;

import java.util.List;


public abstract class Stmt {


	public interface Visitor<R> {
		R visitExpressionStmt(ExpressionStmt stmt);
		R visitPrintStmt(PrintStmt stmt);
		R visitBlockStmt(BlockStmt stmt);
		R visitVarDeclStmt(VarDeclStmt stmt);
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



	public static class PrintStmt extends Stmt{
		public PrintStmt(Expr expression) {
			this.expression = expression;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitPrintStmt(this);
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




	public abstract <R> R accept(Visitor<R> visitor);

}
