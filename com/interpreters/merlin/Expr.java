package com.interpreters.merlin;

import java.util.List;


public abstract class Expr {


	public interface Visitor<R> {
		R visitLiteralExpr(LiteralExpr expr);
		R visitUnaryExpr(UnaryExpr expr);
		R visitGroupingExpr(GroupingExpr expr);
		R visitBinaryExpr(BinaryExpr expr);
		R visitVariableExpr(VariableExpr expr);
		R visitAssignExpr(AssignExpr expr);
		R visitLogicExpr(LogicExpr expr);
		R visitCallExpr(CallExpr expr);
		R visitFunctionExpr(FunctionExpr expr);
		R visitGetExpr(GetExpr expr);
		R visitSetExpr(SetExpr expr);
		R visitThisExpr(ThisExpr expr);
		R visitSuperExpr(SuperExpr expr);
		R visitSuperCallExpr(SuperCallExpr expr);
		R visitListExpr(ListExpr expr);
		R visitListGetExpr(ListGetExpr expr);
		R visitListSetExpr(ListSetExpr expr);
		R visitTernaryExpr(TernaryExpr expr);
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



	public static class AssignExpr extends Expr{
		public AssignExpr(Expr.VariableExpr object, Expr value) {
			this.object = object;
			this.value = value;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitAssignExpr(this);
		}
		public final Expr.VariableExpr object;
		public final  Expr value;
	}



	public static class LogicExpr extends Expr{
		public LogicExpr(Expr left, Token operation, Expr right) {
			this.left = left;
			this.operation = operation;
			this.right = right;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitLogicExpr(this);
		}
		public final Expr left;
		public final  Token operation;
		public final  Expr right;
	}



	public static class CallExpr extends Expr{
		public CallExpr(Expr callee, Token paren, List<Expr> arguments) {
			this.callee = callee;
			this.paren = paren;
			this.arguments = arguments;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitCallExpr(this);
		}
		public final Expr callee;
		public final  Token paren;
		public final  List<Expr> arguments;
	}



	public static class FunctionExpr extends Expr{
		public FunctionExpr(Token paren, List<Token> parameters, List<Stmt> body) {
			this.paren = paren;
			this.parameters = parameters;
			this.body = body;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitFunctionExpr(this);
		}
		public final Token paren;
		public final  List<Token> parameters;
		public final  List<Stmt> body;
	}



	public static class GetExpr extends Expr{
		public GetExpr(Expr object, Token property) {
			this.object = object;
			this.property = property;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitGetExpr(this);
		}
		public final Expr object;
		public final  Token property;
	}



	public static class SetExpr extends Expr{
		public SetExpr(Expr object, Token property, Expr value) {
			this.object = object;
			this.property = property;
			this.value = value;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitSetExpr(this);
		}
		public final Expr object;
		public final  Token property;
		public final  Expr value;
	}



	public static class ThisExpr extends Expr{
		public ThisExpr(Token keyword) {
			this.keyword = keyword;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitThisExpr(this);
		}
		public final Token keyword;
	}



	public static class SuperExpr extends Expr{
		public SuperExpr(Token keyword, Token property) {
			this.keyword = keyword;
			this.property = property;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitSuperExpr(this);
		}
		public final Token keyword;
		public final  Token property;
	}



	public static class SuperCallExpr extends Expr{
		public SuperCallExpr(Token keyword, List<Expr> arguments) {
			this.keyword = keyword;
			this.arguments = arguments;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitSuperCallExpr(this);
		}
		public final Token keyword;
		public final  List<Expr> arguments;
	}



	public static class ListExpr extends Expr{
		public ListExpr(Token bracket, List<Expr> elements) {
			this.bracket = bracket;
			this.elements = elements;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitListExpr(this);
		}
		public final Token bracket;
		public final  List<Expr> elements;
	}



	public static class ListGetExpr extends Expr{
		public ListGetExpr(Expr object, Token bracket, Expr index) {
			this.object = object;
			this.bracket = bracket;
			this.index = index;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitListGetExpr(this);
		}
		public final Expr object;
		public final  Token bracket;
		public final  Expr index;
	}



	public static class ListSetExpr extends Expr{
		public ListSetExpr(Expr.ListGetExpr getter, Expr value) {
			this.getter = getter;
			this.value = value;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitListSetExpr(this);
		}
		public final Expr.ListGetExpr getter;
		public final  Expr value;
	}



	public static class TernaryExpr extends Expr{
		public TernaryExpr(Expr condition, Expr left, Expr right) {
			this.condition = condition;
			this.left = left;
			this.right = right;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitTernaryExpr(this);
		}
		public final Expr condition;
		public final  Expr left;
		public final  Expr right;
	}




	public abstract <R> R accept(Visitor<R> visitor);

}
