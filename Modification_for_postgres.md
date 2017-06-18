### Some quick modification for PostgreSQL

# The problem demo

## MySQL with auto_increment primary key

```mysql

mysql> create table test1 ( 
	id int auto_increment not null  primary key,
	nickname varchar(30),
	sal int
);
Query OK, 0 rows affected

mysql> insert into test1 (id, nickname, sal) values (1, 'feitian', 11);
Query OK, 1 row affected

mysql> select * from test1;
+----+----------+-----+
| id | nickname | sal |
+----+----------+-----+
|  1 | feitian  |  11 |
+----+----------+-----+
1 row in set

mysql> insert into test1 (nickname, sal) values ('feitian1', 111);
Query OK, 1 row affected

mysql> select * from test1;
+----+----------+-----+
| id | nickname | sal |
+----+----------+-----+
|  1 | feitian  |  11 |
|  2 | feitian1 | 111 |
+----+----------+-----+
2 rows in set

mysql> 
```

First, designated the primary key column when inserting a new row, the autoincrement primary key increases.

Second, **not** designated the primary key column when inserting a new row, the autoincrement primary key **also** increases.


## PostgrSQL with serial primary key

```sql

postgres=# create table test1 (
	id serial primary key,
	nickname varchar(30),
	sal int
);
Command OK

postgres=# insert into test1 (id, nickname, sal) values (1, 'feitian', 11);
Command OK - 1 row affected

postgres=# select * from test1;
+----+----------+-----+
| id | nickname | sal |
+----+----------+-----+
|  1 | feitian  |  11 |
+----+----------+-----+
1 row in set

postgres=# insert into test1 (nickname, sal) values ('feitian1', 111);
ERROR:  duplicate key value violates unique constraint "test1_pkey"
DETAIL:  Key (id)=(1) already exists.
postgres=# 

```

First, designated the primary key column when inserting a new row, the autoincrement primary key increases.

Second, **not** designated the primary key column when inserting a new row, you got an unique constraint violation.

Maybe it's feature for postgresql work this way. There're many workaroud ways, but the most dirty way is to modify the source code of jFinal as:

```java
	public void forModelSave(Table table, Map<String, Object> attrs, StringBuilder sql, List<Object> paras) {
		sql.append("insert into \"").append(table.getName()).append("\"(");
		StringBuilder temp = new StringBuilder(") values(");
        String pKey = table.getPrimaryKey();
		for (Entry<String, Object> e: attrs.entrySet()) {
			String colName = e.getKey();
			if (table.hasColumnLabel(colName) && !colName.equalsIgnoreCase(pKey)) {
				if (paras.size() > 0) {
					sql.append(", ");
					temp.append(", ");
				}
                if (colName.equalsIgnoreCase(pKey))
				{
					continue;
				} else {

                    sql.append('\"').append(colName).append('\"');
                    temp.append('?');
                    paras.add(e.getValue());
                }
			}
		}
		sql.append(temp.toString()).append(')');
	}
```

You "escape" the primay key when inserting a new row.

