package com.interpreters.merlin;

import java.util.List;


public abstract class Expr {


	public interface Visitor<R> {
		R visitLiteralExpr(LiteralExpr expr);
		R visitUnaryExpr(UnaryExpr expr);
		R visitGroupingExpr(GroupingExpr expr);
		R visitBinaryExpr(BinaryExpr expr);
		R visitVariableExpr(VariableExpr expr);
	}


	public static class LiteralExpr extends Expr{
		public LiteralExpr(Object value) {
			this.value = value;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitLiteralExpr(this);
		}
		public final Object value;
	}



	public static class UnaryExpr extends Expr{
		public UnaryExpr(Token operation, Expr right) {
			this.operation = operation;
			this.right = right;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitUnaryExpr(this);
		}
		public final Token operation;
		public final  Expr right;
	}



	public static class GroupingExpr extends Expr{
		public GroupingExpr(Expr expression) {
			this.expression = expression;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitGroupingExpr(this);
		}
		public final Expr expression;
	}



	public static class BinaryExpr extends Expr{
		public BinaryExpr(Expr left, Token operation, Expr right) {
			this.left = left;
			this.operation = operation;
			this.right = right;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitBinaryExpr(this);
		}
		public final Expr left;
		public final  Token operation;
		public final  Expr right;
	}



	public static class VariableExpr extends Expr{
		public VariableExpr(Token name) {
			this.name = name;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitVariableExpr(this);
		}
		public final Token name;
	}




	public abstract <R> R accept(Visitor<R> visitor);

}
