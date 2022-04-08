
select * from bbb
where
entity_id in(
#for(x : entityIds)
  #if(for.first)
    '#(x)'
  #else
    ,#(x)
  #end
#end
)
and id = '#(id)' and age > #(age) and day = '#day(day,'-2M', 'YYYYMMED')' and long = '#(long)'
and date = #day(day,'yyyy/MM/ED')
and date = #day()
and date = #day(day)

