package org.rpc.util;

import java.util.Map;

public
interface Mapable
{
  public void fromMap(Map<String, Object> map);
  
  public Map<String, Object> toMap();
}
