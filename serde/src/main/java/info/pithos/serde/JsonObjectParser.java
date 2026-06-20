/*
 * Copyright 2026 Pithos
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package info.pithos.serde;

import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author shilpa
 *
 * Dec 28, 2024
 *
 */
public class JsonObjectParser implements ObjectSerde<Map<String, Object>>{

  private static ObjectMapper objectMapper = new ObjectMapper();

  private final String json;
  
  /**
   * @param json
   */
  public JsonObjectParser(String json) {
    if (json == null || json.isEmpty()) {
      throw new IllegalArgumentException("null | empty json");
    }
    
    this.json = json;
  }
  
  @Override
  public SerdeType getType() {
    return SerdeType.Map;
  }

  @Override
  public Map<String, Object> getObject() {
    Map<String, Object> map;
    try {
      map = objectMapper.readValue(json, new TypeReference<Map<String, Object>>(){});
    } catch (JsonMappingException e) {
      throw new SerdeException("json mapping error", e);
    } catch (JsonProcessingException e) {
      throw new SerdeException("json map processing error", e);
    }
    
    return map;
  }

  @Override
  public String serialize() {
    return this.json;
  }

  @Override
  public String prettyPrint() {
    return this.json;
  }

  @Override
  public byte[] binary() {
    return null;
  }
}
