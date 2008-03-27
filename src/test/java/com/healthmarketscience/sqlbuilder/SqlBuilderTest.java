/*
Copyright (c) 2008 Health Market Science, Inc.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
USA

You can contact Health Market Science at info@healthmarketscience.com
or at the following address:

Health Market Science
2700 Horizon Drive
Suite 200
King of Prussia, PA 19406
*/

package com.healthmarketscience.sqlbuilder;

import java.util.Arrays;

import com.healthmarketscience.sqlbuilder.dbspec.RejoinTable;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbFunction;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbFunctionPackage;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbIndex;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbJoin;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;
import junit.framework.TestCase;
import java.util.Date;


/**
 * SqlBuilderTest.java
 *
 *
 * @author James Ahlborn
 */
public class SqlBuilderTest extends TestCase
{
  private DbSpec _spec;
  private DbSchema _schema1;
  private DbSchema _defSchema;
  private DbTable _table1;
  private DbColumn _table1_col1;
  private DbColumn _table1_col2;
  private DbColumn _table1_col3;
  private DbTable _defTable1;
  private DbColumn _defTable1_col_id;
  private DbColumn _defTable1_col2;
  private DbColumn _defTable1_col3;
  private DbTable _defTable2;
  private DbColumn _defTable2_col_id;
  private DbColumn _defTable2_col4;
  private DbColumn _defTable2_col5;
  
  public SqlBuilderTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception
  {
    _spec = new DbSpec();
    _schema1 = _spec.addSchema("Schema1");
    _defSchema = _spec.addDefaultSchema();
    _table1 = _schema1.addTable("Table1");
    _table1_col1 = _table1.addColumn("col1", "VARCHAR", 213);
    _table1_col2 = _table1.addColumn("col2", "NUMBER", 7);
    _table1_col3 = _table1.addColumn("col3", "TIMESTAMP", null);
    _defTable1 = _defSchema.addTable("Table1");
    _defTable1_col_id = _defTable1.addColumn("col_id", "NUMBER", null);
    _defTable1_col2 = _defTable1.addColumn("col2", "VARCHAR", 64);
    _defTable1_col3 = _defTable1.addColumn("col3", "DATE", null);
    _defTable2 = _defSchema.addTable("Table2");
    _defTable2_col_id = _defTable2.addColumn("col_id");
    _defTable2_col4 = _defTable2.addColumn("col4");
    _defTable2_col5 = _defTable2.addColumn("col5");
  }

  public void testCreateTable()
  {
    String createStr1 = new CreateTableQuery(_table1)
      .addColumns(_table1_col1, _table1_col3).validate().toString();
    checkResult(createStr1,
                "CREATE TABLE Schema1.Table1 (col1 VARCHAR(213),col3 TIMESTAMP)");
    
    String createStr2 = new CreateTableQuery(_table1, true)
      .validate().toString();
    checkResult(createStr2,
                "CREATE TABLE Schema1.Table1 (col1 VARCHAR(213),col2 NUMBER(7),col3 TIMESTAMP)");

    String createStr3 = new CreateTableQuery(_defTable1, true)
      .validate().toString();
    checkResult(createStr3,
                "CREATE TABLE Table1 (col_id NUMBER,col2 VARCHAR(64),col3 DATE)");

    String createStr4 = new CreateTableQuery(_defTable1, true)
      .setColumnConstraint(_defTable1_col_id,
                           CreateTableQuery.ColumnConstraint.PRIMARY_KEY)
      .setColumnConstraint(_defTable1_col3,
                           CreateTableQuery.ColumnConstraint.NOT_NULL)
      .validate().toString();
    checkResult(createStr4,
                "CREATE TABLE Table1 (col_id NUMBER PRIMARY KEY,col2 VARCHAR(64),col3 DATE NOT NULL)");

    try {
      new CreateTableQuery(_table1).validate();
      fail("ValidationException should have been thrown");
    } catch(ValidationException e) {}
  }

  public void testCreateIndex()
  {
    DbIndex index = _schema1.addIndex("Index1", "Table1",
                                      "col1", "col2");
    
    String createStr1 = new CreateIndexQuery(index).validate().toString();
    checkResult(createStr1,
                "CREATE INDEX Schema1.Index1 ON Schema1.Table1 (col1,col2)");
  }
  
  public void testDropTable()
  {
    String dropStr1 = DropQuery.dropTable(_table1).validate().toString();
    checkResult(dropStr1, "DROP TABLE Schema1.Table1");

    String dropStr2 = DropQuery.dropTable(_defTable1)
      .setBehavior(DropQuery.Behavior.CASCADE).validate().toString();
    checkResult(dropStr2, "DROP TABLE Table1 CASCADE");

    String dropStr3 = new CreateTableQuery(_table1)
      .addColumns(_table1_col1, _table1_col3)
      .getDropQuery().validate().toString();
    checkResult(dropStr3, "DROP TABLE Schema1.Table1");
    
    String dropStr4 = new CreateTableQuery(_defTable1, true)
      .getDropQuery().validate().toString();
    checkResult(dropStr4, "DROP TABLE Table1");
  }

  public void testInsert()
  {
    String insertStr1 = new InsertQuery(_table1)
      .addColumns(new DbColumn[]{_table1_col1, _table1_col3},
                  new Object[]{new Integer(13), new String("feed me seymor")})
      .validate().toString();
    checkResult(insertStr1,
                "INSERT INTO Schema1.Table1 (col1,col3) VALUES ('13','feed me seymor')");
    
    String insertStr2 = new InsertQuery(_table1)
      .addColumns(new DbColumn[]{_table1_col1},
                  new Object[]{new Integer(13)})
      .addPreparedColumns(_table1_col2, _table1_col3)
      .validate().toString();
    checkResult(insertStr2,
                "INSERT INTO Schema1.Table1 (col1,col2,col3) VALUES ('13',?,?)");

    String insertStr3 = new InsertQuery(_defTable1)
      .addColumns(new DbColumn[]{_defTable1_col_id},
                  new Object[]{new Integer(13)})
      .addPreparedColumns(_defTable1_col2, _defTable1_col3)
      .validate().toString();
    checkResult(insertStr3,
                "INSERT INTO Table1 (col_id,col2,col3) VALUES ('13',?,?)");
    
    try {
      new InsertQuery(_table1)
        .addColumns(new DbColumn[]{_table1_col1, _table1_col3},
                    new Object[]{new Integer(13)})
        .validate();
      fail("ValidationException should have been thrown");
    } catch(ValidationException e) {}
  }

  public void testInsertSelect()
  {
    SelectQuery selectQuery = new SelectQuery()
      .addColumns(_table1_col1, _table1_col2, _table1_col3).validate();

    String insertStr1 = new InsertSelectQuery(_defTable1)
      .addColumns(_defTable1_col_id, _defTable1_col2,
                  _defTable1_col3)
      .setSelectQuery(selectQuery)
      .validate().toString();
    checkResult(insertStr1,
                "INSERT INTO Table1 (col_id,col2,col3) SELECT t0.col1,t0.col2,t0.col3 FROM Schema1.Table1 t0");

    try {
      new InsertSelectQuery(_defTable1)
        .addColumns(_defTable1_col_id, _defTable1_col2,
                    _defTable1_col3)
        .validate();
      fail("ValidationException should have been thrown");
    } catch(ValidationException e) {}

    try {
      new InsertSelectQuery(_defTable1)
        .addColumns(_defTable1_col_id, _defTable1_col2)
        .setSelectQuery(selectQuery)
        .validate();
      fail("ValidationException should have been thrown");
    } catch(ValidationException e) {}
  }

  public void testSelect()
  {
    // add some joins to use in our selects
    DbJoin idJoin = _spec.addJoin(null, "Table1",
                                  null, "Table2",
                                  "col_id");

    _table1.addColumn("col4");
    _defTable1.addColumn("altCol4");
    
    DbJoin col4Join = _spec.addJoin("Schema1", "Table1",
                                    null, "Table1",
                                    new String[]{"col4"},
                                    new String[]{"altCol4"});
      
    {
      SelectQuery selectQuery1 = new SelectQuery()
        .addColumns(_table1_col1, _defTable1_col2, _defTable2_col5);

      String selectStr1 = selectQuery1.validate().toString();
      checkResult(selectStr1,
                "SELECT t0.col1,t1.col2,t2.col5 FROM Schema1.Table1 t0,Table1 t1,Table2 t2");

      String selectStr2 = selectQuery1.setIsDistinct(true)
        .validate().toString();
      checkResult(selectStr2,
                  "SELECT DISTINCT t0.col1,t1.col2,t2.col5 FROM Schema1.Table1 t0,Table1 t1,Table2 t2");

      String selectStr3 = selectQuery1.addJoins(SelectQuery.JoinType.INNER,
                                                col4Join)
        .addJoins(SelectQuery.JoinType.LEFT_OUTER, idJoin)
        .validate().toString();
      checkResult(selectStr3,
                  "SELECT DISTINCT t0.col1,t1.col2,t2.col5 FROM Schema1.Table1 t0 INNER JOIN Table1 t1 ON (t0.col4 = t1.altCol4) LEFT OUTER JOIN Table2 t2 ON (t1.col_id = t2.col_id)");

      String selectStr4 = selectQuery1.addOrderings(_defTable1_col2)
        .validate().toString();
      checkResult(selectStr4,
                  "SELECT DISTINCT t0.col1,t1.col2,t2.col5 FROM Schema1.Table1 t0 INNER JOIN Table1 t1 ON (t0.col4 = t1.altCol4) LEFT OUTER JOIN Table2 t2 ON (t1.col_id = t2.col_id) ORDER BY t1.col2");

      String selectStr5 = selectQuery1.addCondition(
        BinaryCondition.greaterThan(_defTable2_col4, 42, true))
        .validate().toString();
      checkResult(selectStr5,
                  "SELECT DISTINCT t0.col1,t1.col2,t2.col5 FROM Schema1.Table1 t0 INNER JOIN Table1 t1 ON (t0.col4 = t1.altCol4) LEFT OUTER JOIN Table2 t2 ON (t1.col_id = t2.col_id) WHERE (t2.col4 >= '42') ORDER BY t1.col2");

      String selectStr6 = selectQuery1.addOrdering(_defTable2_col5,
                                                   OrderObject.Dir.DESCENDING)
        .validate().toString();
      checkResult(selectStr6,
                  "SELECT DISTINCT t0.col1,t1.col2,t2.col5 FROM Schema1.Table1 t0 INNER JOIN Table1 t1 ON (t0.col4 = t1.altCol4) LEFT OUTER JOIN Table2 t2 ON (t1.col_id = t2.col_id) WHERE (t2.col4 >= '42') ORDER BY t1.col2,t2.col5 DESC");

      String selectStr7 = selectQuery1.addHaving(BinaryCondition.greaterThan(_defTable1_col2, new NumberValueObject(1), false)).toString();
      checkResult(selectStr7,
                  "SELECT DISTINCT t0.col1,t1.col2,t2.col5 FROM Schema1.Table1 t0 INNER JOIN Table1 t1 ON (t0.col4 = t1.altCol4) LEFT OUTER JOIN Table2 t2 ON (t1.col_id = t2.col_id) WHERE (t2.col4 >= '42') ORDER BY t1.col2,t2.col5 DESC");
      
      String selectStr8 = selectQuery1.addGroupings(_defTable1_col2,
                                                    _defTable2_col5)
        .validate().toString();
      checkResult(selectStr8,
                  "SELECT DISTINCT t0.col1,t1.col2,t2.col5 FROM Schema1.Table1 t0 INNER JOIN Table1 t1 ON (t0.col4 = t1.altCol4) LEFT OUTER JOIN Table2 t2 ON (t1.col_id = t2.col_id) WHERE (t2.col4 >= '42') GROUP BY t1.col2,t2.col5 HAVING (t1.col2 > 1) ORDER BY t1.col2,t2.col5 DESC");
    }

    String selectStr6 = new SelectQuery()
      .addAllTableColumns(_table1)
      .validate().toString();
    checkResult(selectStr6,
                "SELECT t0.* FROM Schema1.Table1 t0");

    String selectStr7 = new SelectQuery()
      .addAllColumns()
      .addFromTable(_defTable1)
      .addFromTable(_defTable2)
      .validate().toString();
    checkResult(selectStr7,
                "SELECT * FROM Table1 t1, Table2 t2");

    String selectStr8 = new SelectQuery()
      .setForUpdate(true)
      .addAllColumns()
      .addFromTable(_defTable1)
      .addFromTable(_defTable2)
      .validate().toString();
    checkResult(selectStr8,
                "SELECT * FROM Table1 t1, Table2 t2 FOR UPDATE");

    String selectStr9 = new SelectQuery()
      .addColumns(_table1_col1, _defTable1_col2)
      .addCustomColumns(Converter.toColumnSqlObject(
                            _defTable2_col5, "MyCol"))
      .validate().toString();
    checkResult(selectStr9,
                "SELECT t0.col1,t1.col2,t2.col5 AS MyCol FROM Schema1.Table1 t0,Table1 t1,Table2 t2");

    
    try {
      new SelectQuery()
        .addColumns(_table1_col1, _defTable1_col2, _defTable2_col5)
        .addFromTable(_defTable1).validate();
      fail("ValidationException should have been thrown");
    } catch(ValidationException e) {}
      
    try {
      new SelectQuery()
        .addColumns(_defTable1_col2)
        .addFromTable(_defTable1)
        .addOrderings(_table1_col1)
        .validate();
      fail("ValidationException should have been thrown");
    } catch(ValidationException e) {}
      
    try {
      new SelectQuery()
        .addCustomColumns(new CustomSql("col1"), new CustomSql("col2"))
        .validate();
      fail("ValidationException should have been thrown");
    } catch(ValidationException e) {}

    try {
      new SelectQuery()
        .addColumns(_defTable1_col2)
        .addFromTable(_defTable1)
        .addIndexedOrderings(2)
        .validate();
      fail("ValidationException should have been thrown");
    } catch(ValidationException e) {}

    try {
      new SelectQuery()
        .addColumns(_defTable1_col_id, _defTable1_col2)
        .addFromTable(_defTable1)
        .addCustomOrderings(1.5d)
        .validate();
      fail("ValidationException should have been thrown");
    } catch(ValidationException e) {}

    try {
      DbTable table3 = _schema1.addTable("Table3");
      
      new SelectQuery()
        .addColumns(_table1_col1)
        .addJoin(SelectQuery.JoinType.INNER, _table1, _defTable1,
                 Arrays.asList(_table1_col1),
                 Arrays.asList(_defTable1_col_id))
        .addJoin(SelectQuery.JoinType.INNER, table3, _defTable2,
                 Arrays.asList(_defTable1_col_id),
                 Arrays.asList(_defTable2_col_id))
        .validate();
      fail("ValidationException should have been thrown");
    } catch(ValidationException e) {}
      
  }

  public void testCondition()
  {
    String reallyComplicatedConditionStr = ComboCondition.and(
      BinaryCondition.lessThan(_table1_col1, "FOO", false),
      ComboCondition.or(),
      UnaryCondition.isNotNull(_defTable1_col_id),
      new ComboCondition(ComboCondition.Op.OR,
                         new CustomCondition("IM REALLY SNAZZY"),
                         new NotCondition(
                           BinaryCondition.like(_defTable2_col5,
                                                "BUZ%")),
                         new BinaryCondition(BinaryCondition.Op.EQUAL_TO,
                                             new CustomSql("YOU"),
                                             "ME")),
      ComboCondition.or(
        new UnaryCondition(UnaryCondition.Op.IS_NULL,
                           _table1_col2)),
      new InCondition(_defTable2_col4,
                      "this string",
                      new NumberValueObject(37))
      .addObject(new NumberValueObject(42)),
      BinaryCondition.notLike(_table1_col2, "\\_%").setLikeEscapeChar('\\'))
      .toString();
    checkResult(reallyComplicatedConditionStr,
                "((t0.col1 < 'FOO') AND (t1.col_id IS NOT NULL) AND ((IM REALLY SNAZZY) OR (NOT (t2.col5 LIKE 'BUZ%')) OR (YOU = 'ME')) AND (t0.col2 IS NULL) AND (t2.col4 IN ('this string',37,42) ) AND (t0.col2 NOT LIKE '\\_%' ESCAPE '\\'))");

    try {
      BinaryCondition.equalTo(_table1_col2, "\\37").setLikeEscapeChar('\\');
      fail("IllegalArgumentException should have been thrown");
    } catch(IllegalArgumentException e) {}
  }

  public void testExpression()
  {
    String reallyComplicatedExpression = ComboExpression.add(
        37, _defTable2_col5,
        new NegateExpression(
            ComboExpression.multiply(_table1_col1, 4.7f)),
        ComboExpression.subtract(),
        "PI", new CustomSql("8 - 3"))
      .toString();
    checkResult(reallyComplicatedExpression,
                "(37 + t2.col5 + (- (t0.col1 * 4.7)) + 'PI' + (8 - 3))");

    String concatExpression = ComboExpression.concatenate(
        "The answer is ", ComboExpression.add(40, 2), ".")
      .toString();
    checkResult(concatExpression,
                "('The answer is ' || (40 + 2) || '.')");
  }

  public void testFunction()
  {
    // add some functions to play with
    DbFunctionPackage funcPack1 = _schema1.addFunctionPackage("fpkg");
    DbFunction func1 = funcPack1.addFunction("func1");
    DbFunctionPackage funcPack2 = _schema1.addFunctionPackage(null);
    DbFunction func2 = funcPack2.addFunction("Func2");
    DbFunctionPackage funcPack3 = _defSchema.addDefaultFunctionPackage();
    DbFunction func3 = funcPack3.addFunction("func3");
    
    String funcStr1 = new FunctionCall(func1).toString();
    checkResult(funcStr1, "Schema1.fpkg.func1()");

    String funcStr2 = new FunctionCall(func2)
      .addColumnParams(_table1_col1)
      .toString();
    checkResult(funcStr2, "Schema1.Func2(t0.col1)");
    
    String funcStr3 = new FunctionCall(func2)
      .setIsDistinct(true)
      .addColumnParams(_table1_col1)
      .toString();
    checkResult(funcStr3, "Schema1.Func2(DISTINCT t0.col1)");
    
    String funcStr4 = new FunctionCall(func3)
      .addColumnParams(_table1_col1)
      .addCustomParams(42)
      .toString();
    checkResult(funcStr4, "func3(t0.col1,'42')");
    
    String funcStr5 = new FunctionCall(func3)
      .addCustomParams(new String("HAPPY"), _table1_col1)
      .toString();
    checkResult(funcStr5, "func3('HAPPY',t0.col1)");

    String funcStr6 = FunctionCall.sum()
      .addColumnParams(_table1_col3)
      .toString();
    checkResult(funcStr6, "SUM(t0.col3)");

    String funcStr7 = FunctionCall.countAll()
      .toString();
    checkResult(funcStr7, "COUNT(*)");

    String funcStr8 = new FunctionCall(func3)
      .addColumnParams(_table1_col1)
      .addNumericValueParam(42)
      .toString();
    checkResult(funcStr8, "func3(t0.col1,42)");
    
  }

  public void testCustom()
  {
    String customStr1 = new SelectQuery()
      .addColumns(_defTable1_col_id)
      .addFromTable(_defTable1)
      .addCustomFromTable(new CustomSql("otherTable"))
      .addCustomColumns(new CustomSql("fooCol"), new CustomSql("BazzCol"))
      .addCondition(ComboCondition.and(
                      new BinaryCondition(BinaryCondition.Op.LESS_THAN,
                                          new CustomSql("fooCol"),
                                          new ValueObject(37)),
                      new CustomCondition("bazzCol IS FUNKY")))
      .validate().toString();
    checkResult(customStr1,
                "SELECT t1.col_id,fooCol,BazzCol FROM Table1 t1, otherTable WHERE ((fooCol < '37') AND (bazzCol IS FUNKY))");
  }

  public void testPreparer()
  {
    doTestPreparer(1);
    doTestPreparer(13);
  }

  private void doTestPreparer(int startIndex)
  {
    QueryPreparer prep = new QueryPreparer(startIndex);
    QueryPreparer.PlaceHolder ph1 = prep.getNewPlaceHolder();
    QueryPreparer.PlaceHolder ph2 = prep.getNewPlaceHolder();
    QueryPreparer.PlaceHolder ph3 = prep.getNewPlaceHolder();
    QueryPreparer.PlaceHolder sph1 = prep.addStaticPlaceHolder(42);
    QueryPreparer.MultiPlaceHolder mph1 = prep.getNewMultiPlaceHolder();

    String reallyComplicatedConditionStr = ComboCondition.and(
      BinaryCondition.lessThan(_table1_col1, ph1, false),
      BinaryCondition.lessThan(_table1_col2, mph1, true),
      UnaryCondition.isNotNull(_defTable1_col_id),
      new ComboCondition(ComboCondition.Op.OR,
                         new CustomCondition("IM REALLY SNAZZY"),
                         new NotCondition(
                           BinaryCondition.like(_defTable2_col5,
                                                ph2)),
                         new BinaryCondition(BinaryCondition.Op.EQUAL_TO,
                                             new CustomSql("YOU"),
                                             sph1)),
      ComboCondition.or(
        new UnaryCondition(UnaryCondition.Op.IS_NULL,
                           _table1_col2),
        BinaryCondition.notEqualTo(mph1, mph1))).toString();
    checkResult(reallyComplicatedConditionStr,
                "((t0.col1 < ?) AND (t0.col2 <= ?) AND (t1.col_id IS NOT NULL) AND ((IM REALLY SNAZZY) OR (NOT (t2.col5 LIKE ?)) OR (YOU = ?)) AND ((t0.col2 IS NULL) OR (? <> ?)))");

    assertEquals((0 + startIndex), ph1.getIndex());
    assertEquals((2 + startIndex), ph2.getIndex());
    assertEquals(false, ph3.isInQuery());
    assertEquals((3 + startIndex), sph1.getIndex());
    assertEquals(3, mph1.getIndexes().size());
    assertEquals(Arrays.asList((1 + startIndex), (4 + startIndex),
                               (5 + startIndex)),
                 mph1.getIndexes());
  }

  public void testReader() {
    doTestReader(1);
    doTestReader(13);
  }

  private void doTestReader(int startIndex)
  {
    QueryReader prep = new QueryReader(startIndex);
    QueryReader.Column col1 = prep.getNewColumn();
    QueryReader.Column col2 = prep.getNewColumn();
    QueryReader.Column col3 = prep.getNewColumn();
    QueryReader.Column col4 = prep.getNewColumn();

    String selectStr = new SelectQuery()
      .addCustomColumns(
          col1.setColumnObject(_table1_col1),
          col4.setColumnObject(_table1_col3),
          col3.setCustomColumnObject(new CustomSql("foo")))
      .validate().toString();

    checkResult(selectStr,
                "SELECT t0.col1,t0.col3,foo FROM Schema1.Table1 t0");
    
    assertEquals((0 + startIndex), col1.getIndex());
    assertEquals((1 + startIndex), col4.getIndex());
    assertEquals(false, col2.isInQuery());
    assertEquals((2 + startIndex), col3.getIndex());
    
  }

  public void testCaseStatement() {
    String caseClause1 = new SimpleCaseStatement(_table1_col1)
      .addNumericWhen(1, "one")
      .addNumericWhen(2, "two")
      .addElse("three").toString();

    checkResult(caseClause1,
                "(CASE t0.col1 WHEN 1 THEN 'one' WHEN 2 THEN 'two' ELSE 'three' END)");

    String caseClause2 = new CaseStatement()
      .addWhen(BinaryCondition.equalTo(_table1_col2, "13"), _table1_col3)
      .addWhen(BinaryCondition.equalTo(_table1_col2, "14"), "14")
      .addElseNull().toString();
    
    checkResult(caseClause2,
                "(CASE WHEN (t0.col2 = '13') THEN t0.col3 WHEN (t0.col2 = '14') THEN '14' ELSE NULL END)");

    String caseClause3 = new SimpleCaseStatement(_table1_col2)
      .toString();
    checkResult(caseClause3, "");
  }

  public void testDelete()
  {
    String deleteQuery1 = new DeleteQuery(_table1)
      .addCondition(BinaryCondition.equalTo(_table1_col2, "13"))
      .validate().toString();

    checkResult(deleteQuery1,
                "DELETE FROM Schema1.Table1 WHERE (col2 = '13')");
    
  }

  public void testUpdate()
  {
    String updateQuery1 = new UpdateQuery(_table1)
      .addSetClause(_table1_col1, 47)
      .addSetClause(_table1_col3, "foo")
      .addCondition(BinaryCondition.equalTo(_table1_col2, "13"))
      .validate().toString();

    checkResult(updateQuery1,
                "UPDATE Schema1.Table1 SET col1 = '47',col3 = 'foo' WHERE (col2 = '13')");
    
  }

  public void testUnion()
  {
    SelectQuery q1 = new SelectQuery()
      .addColumns(_table1_col1, _table1_col2, _table1_col3);
    SelectQuery q2 = new SelectQuery()
      .addColumns(_defTable2_col_id, _defTable2_col4, _defTable2_col5);

    UnionQuery unionQuery = UnionQuery.unionAll(q1, q2);

    String unionQuery1 = unionQuery.validate().toString();
    checkResult(unionQuery1,
                "SELECT t0.col1,t0.col2,t0.col3 FROM Schema1.Table1 t0 UNION ALL SELECT t2.col_id,t2.col4,t2.col5 FROM Table2 t2");

    q1.addColumns(_defTable1_col3);
    try {
      unionQuery.validate();
      fail("ValidationException should have been thrown");
    } catch(ValidationException e) {}

    q1 = new SelectQuery()
      .addColumns(_table1_col1, _table1_col2, _table1_col3);
    q2 = new SelectQuery()
      .addColumns(_defTable2_col_id, _defTable2_col4, _defTable2_col5);

    unionQuery = UnionQuery.unionAll(q1, q2);
    unionQuery.addIndexedOrderings(1);
    unionQuery.addOrderings(_table1_col1);

    String unionQuery2 = unionQuery.validate().toString();
    checkResult(unionQuery2,
                "SELECT t0.col1,t0.col2,t0.col3 FROM Schema1.Table1 t0 UNION ALL SELECT t2.col_id,t2.col4,t2.col5 FROM Table2 t2 ORDER BY 1,col1");

    q1.addOrderings(_table1_col2);
    try {
      unionQuery.validate();
      fail("ValidationException should have been thrown");
    } catch(ValidationException e) {}

  }

  public void testSqlContext()
  {
    SqlContext context = new SqlContext();
    Condition cond = ComboCondition.and(
        BinaryCondition.equalTo(_defTable1_col3, "foo"),
        BinaryCondition.lessThan(_table1_col1, 13, true));

    String condStr1 = cond.toString(32, context);
    checkResult(condStr1,
                "((t1.col3 = 'foo') AND (t0.col1 <= '13'))");

    context.setUseTableAliases(false);
    String condStr2 = cond.toString(32, context);
    checkResult(condStr2,
                "((col3 = 'foo') AND (col1 <= '13'))");
    
  }

  public void testRejoinTable()
  {
    RejoinTable rejoinTable1 = new RejoinTable(_table1, "t5");
    assertSame(_table1, rejoinTable1.getOriginalTable());
    assertSame(_table1_col1,
               rejoinTable1.getColumns().get(0).getOriginalColumn());
    String rejoinQuery = (new SelectQuery())
      .addFromTable(_table1)
      .addColumns(_table1_col1, _table1_col2)
      .addFromTable(rejoinTable1)
      .addColumns(rejoinTable1.getColumns().get(0),
                  rejoinTable1.getColumns().get(1))
      .validate().toString();

    checkResult(rejoinQuery,
                "SELECT t0.col1,t0.col2,t5.col1,t5.col2 FROM Schema1.Table1 t0, Schema1.Table1 t5");
  }
  
  public void testJdbcEscape()
  {
    String escapeStr1 = new InsertQuery(_table1)
      .addColumns(new DbColumn[]{_table1_col1, _table1_col3},
                  new Object[]{new Integer(13), JdbcScalarFunction.NOW})
      .validate().toString();
    checkResult(escapeStr1,
                "INSERT INTO Schema1.Table1 (col1,col3) VALUES ('13',{fn NOW()})");
    
    Date d = new Date(1204909500692L);
    String dateStr = JdbcEscape.date(d).toString();
    checkResult(dateStr, "{d '2008-03-07'}");
    String timeStr = JdbcEscape.time(d).toString();
    checkResult(timeStr, "{t '12:05:00'}");
    String timestampStr = JdbcEscape.timestamp(d).toString();
    checkResult(timestampStr, "{ts '2008-03-07 12:05:00.692'}");
  }
  
  public void testGrantRevoke()
  {
    String grantStr1 = new GrantQuery()
      .setTarget(GrantQuery.targetTable(_table1))
      .addPrivileges(GrantQuery.privilegeInsert(_table1_col1),
                     GrantQuery.privilegeUsage())
      .addGrantees("bob", "Mark")
      .validate().toString();
    checkResult(grantStr1, "GRANT INSERT(col1),USAGE ON TABLE Schema1.Table1 TO bob,Mark");

    String revokeStr1 = new RevokeQuery()
      .setTarget(GrantQuery.targetTable(_table1))
      .addPrivileges(GrantQuery.privilegeInsert(_table1_col1),
                     GrantQuery.privilegeUsage())
      .addCustomGrantees(RevokeQuery.PUBLIC_GRANTEE)
      .validate().toString();
    checkResult(revokeStr1, "REVOKE INSERT(col1),USAGE ON TABLE Schema1.Table1 FROM PUBLIC");

    try {
      new GrantQuery()
        .setTarget(GrantQuery.targetTable(_table1))
        .addPrivileges(GrantQuery.privilegeInsert(_defTable1_col3))
        .addGrantees("bob")
        .validate();
      fail("ValidationException should have been thrown");
    } catch(ValidationException e) {}      
  }

  public void testSubquery()
  {
    String queryStr =
      new SelectQuery()
      .addColumns(_table1_col1, _table1_col2)
      .addCondition(new InCondition(
                        _table1_col1, new Subquery(
                            new SelectQuery()
                            .addColumns(_defTable1_col3)
                            .validate())))
      .validate().toString();
    checkResult(queryStr, "SELECT t0.col1,t0.col2 FROM Schema1.Table1 t0,Table1 t1 WHERE (t0.col1 IN ((SELECT t1.col3 FROM Table1 t1)) )");
  }

  public void testAlterTable()
  {
    String queryStr1 =
      new AlterTableQuery(_table1)
      .setAction(new AlterTableQuery.AddUniqueConstraintAction()
                 .addColumns(_table1_col2))
      .validate().toString();
    checkResult(queryStr1, "ALTER TABLE Schema1.Table1 ADD UNIQUE (col2)");

    String queryStr2 =
      new AlterTableQuery(_defTable1)
      .setAction(new AlterTableQuery.AddPrimaryConstraintAction()
                 .addColumns(_defTable1_col_id))
      .validate().toString();
    checkResult(queryStr2, "ALTER TABLE Table1 ADD PRIMARY KEY (col_id)");

    String queryStr3 =
      new AlterTableQuery(_defTable1)
      .setAction(new AlterTableQuery.AddForeignConstraintAction(_defTable2)
                 .addPrimaryKeyReference(_defTable1_col_id))
      .validate().toString();
    checkResult(queryStr3, 
                "ALTER TABLE Table1 ADD FOREIGN KEY (col_id) REFERENCES Table2");

    String queryStr4 =
      new AlterTableQuery(_defTable1)
      .setAction(new AlterTableQuery.AddForeignConstraintAction(_defTable2)
                 .addReference(_defTable1_col_id, _defTable2_col4)
                 .addReference(_defTable1_col2, _defTable2_col5))
      .validate().toString();
    checkResult(queryStr4, 
                "ALTER TABLE Table1 ADD FOREIGN KEY (col_id,col2) " +
                "REFERENCES Table2 (col4,col5)");

  }

  public void testComment()
  {
    String queryStr1 = new SelectQuery()
      .addCustomColumns(_table1_col1, _table1_col2, new Comment("foo bar"))
                        .validate().toString();
    checkResult(queryStr1, "SELECT t0.col1,t0.col2, -- foo bar\n FROM Schema1.Table1 t0");

    String queryStr2 = new SelectQuery()
      .addCustomColumns(_table1_col1, _table1_col2)
                        .validate().toString() +
      new Comment("My coolest query ever");
    checkResult(queryStr2, "SELECT t0.col1,t0.col2 FROM Schema1.Table1 t0 -- My coolest query ever\n");
    
  }

  public void testEscapeLiteral()
  {
    String orig = "/this%is_a ' literal/pattern";

    assertEquals("//this/%is/_a ' literal//pattern",
                 BinaryCondition.escapeLikeLiteral(orig, '/'));
    assertEquals("/this\\%is\\_a ' literal/pattern",
                 BinaryCondition.escapeLikeLiteral(orig, '\\'));
  }
  
  private void checkResult(String result, String expected)
  {
    assertEquals(expected, result);
  }

}
