package org.folio.marccat.integration.search;

import net.sf.hibernate.Session;
import org.folio.marccat.exception.DataAccessException;
import org.folio.marccat.dao.DAOIndexList;
import org.folio.marccat.dao.persistence.IndexList;
import org.folio.marccat.config.log.Log;
import org.folio.marccat.config.log.MessageCatalog;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * Query Parser.
 *
 * @author paulm
 * @author cchiama
 * @since 1.0
 */
public class Parser {
  private static final Log logger = new Log(Parser.class);
  private static IndexList defaultIndex;
  private final Locale locale;
  private final int mainLibraryId;
  private final int searchingView;
  private final Session session;
  private final DAOIndexList dao = new DAOIndexList();
  private LinkedList <Token> tokens;
  private Token lookahead;

  /**
   * Builds a new parser with the given data.
   *
   * @param locale        the current locale.
   * @param mainLibraryId the main library identifier.
   * @param searchingView the current search view.
   */
  public Parser(final Locale locale, final int mainLibraryId, final int searchingView, final Session session) {
    this.locale = locale;
    this.mainLibraryId = mainLibraryId;
    this.searchingView = searchingView;
    this.session = session;
  }

  /**
   * Parses the incoming CCL query.
   *
   * @param ccl the CCL query.
   * @return the parsed string.
   * @throws CclParserException in case of parsing failure.
   */
  public String parse(final String ccl) throws CclParserException {
    final Tokenizer tokenizer = new Tokenizer().tokenize(ccl);

    final ExpressionNode n = parse(tokenizer.getTokens());

    final String query = "select * from ((" + n.getValue() + ")) foo order by 1 desc";
    logger.debug(
      MessageCatalog._00020_SE_QUERY,
      ccl, query);

    return query;
  }

  /**
   * Parses the a list of tokens.
   *
   * @param tokens the tokens list.
   * @return the expression node..
   * @throws CclParserException in case of parsing failure.
   */
  public ExpressionNode parse(final List <Token> tokens) throws CclParserException {
    this.tokens = new LinkedList <>(tokens);
    this.lookahead = this.tokens.getFirst();
    return searchGroup();
  }

  /**
   * Advance to the next token.
   */
  private void nextToken() {
    tokens.poll();
    lookahead =
      tokens.isEmpty()
        ? new Token(Tokenizer.TokenType.EOL, "")
        : tokens.getFirst();
  }

  private ExpressionNode searchGroup() throws CclParserException {
    // ( <searchGroup> )
    if (lookahead.token == Tokenizer.TokenType.LP) {
      nextToken();

      final ExpressionNode expr = searchGroup();
      if (lookahead.token != Tokenizer.TokenType.RP) {
        throw new CclParserException("Mismatched parentheses");
      }

      return expr;
    } else {
      // <searchExpression> <BOOL> <searchGroup>
      final ExpressionNode expr = searchExpression();
      if (lookahead.token == Tokenizer.TokenType.BOOL) {
        final BooleanExpressionNode op = new BooleanExpressionNode();
        op.setLeft(expr);
        op.setOp(lookahead.sequence);

        nextToken();

        op.setRight(searchGroup());

        return op;
      } else {
        return expr;
      }
    }
  }

  private ExpressionNode searchExpression() throws CclParserException {
    final TermExpressionNode expr = new TermExpressionNode(session, locale, mainLibraryId, searchingView);
    if (lookahead.token == Tokenizer.TokenType.WORD) {
      if (lookahead.sequence.length() <= 3) {
        IndexList i;
        try {
          i = dao.getIndexByLocalAbbreviation(session, lookahead.sequence, locale);
        } catch (DataAccessException e) {
          throw new RuntimeException(e);
        }
        if (i != null) {
          expr.setIndex(i);
          lookahead.token = Tokenizer.TokenType.INDEX;
        } else {
          expr.setIndex(getDefaultIndex());
          return term(expr);
        }
      } else {
        expr.setIndex(getDefaultIndex());
        return term(expr);
      }
    }
    if (lookahead.token == Tokenizer.TokenType.INDEX) {
      // <INDEX> [<REL>] <term>
      nextToken();
      if (lookahead.token == Tokenizer.TokenType.REL) {
        expr.setRelation(lookahead.sequence);
        nextToken();
      }
      return term(expr);
    } else {
      // <term>
      return term(expr);
    }
  }

  private IndexList getDefaultIndex() {
    if (defaultIndex == null) {
      try {
        defaultIndex = dao.getIndexByLocalAbbreviation(session, "AW", Locale.ENGLISH);
      } catch (DataAccessException e) {
        throw new RuntimeException(e);
      }
    }
    return defaultIndex;
  }

  private ExpressionNode term(TermExpressionNode expr) throws CclParserException {
    if (lookahead.token == Tokenizer.TokenType.WORD) {
      // <WORD> <wordlist>
      expr.appendToTerm(lookahead.sequence + " ");
      nextToken();
      return wordlist(expr);
    } else {
      return quotedString(expr);
    }
  }

  private ExpressionNode wordlist(TermExpressionNode expr) throws CclParserException {
    if (lookahead.token == Tokenizer.TokenType.PROX) {
      expr.setProximityOperator(lookahead.sequence);
      nextToken();
      if (lookahead.token != Tokenizer.TokenType.WORD) {
        throw new CclParserException("proximity operator has invalid arguments");
      } else {
        expr.setRight(lookahead.sequence);
        nextToken();
        return expr;
      }
    }
    if (lookahead.token == Tokenizer.TokenType.WORD) {
      expr.appendToTerm(lookahead.sequence + " ");
      nextToken();
      return wordlist(expr);
    } else {
      // EOL
      return expr;
    }
  }

  private ExpressionNode quotedString(TermExpressionNode expr) {
    if (lookahead.token == Tokenizer.TokenType.QUOTEDSTRING) {
      String noQuotes = lookahead.sequence.replace("\"", "");
      expr.appendToTerm(noQuotes);
      nextToken();
      return quotedString(expr);
    } else {
      // EOL
      return expr;
    }
  }
}
