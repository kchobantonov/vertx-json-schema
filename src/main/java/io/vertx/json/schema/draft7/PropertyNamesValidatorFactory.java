/*
 * Copyright (c) 2011-2020 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package io.vertx.json.schema.draft7;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.json.schema.NoSyncValidationException;
import io.vertx.json.schema.ValidationException;
import io.vertx.json.schema.common.BaseSingleSchemaValidator;
import io.vertx.json.schema.common.BaseSingleSchemaValidatorFactory;
import io.vertx.json.schema.common.MutableStateValidator;
import io.vertx.json.schema.common.ValidatorContext;

import java.util.Map;
import java.util.stream.Collectors;

public class PropertyNamesValidatorFactory extends BaseSingleSchemaValidatorFactory {

  @Override
  protected BaseSingleSchemaValidator instantiate(MutableStateValidator parent) {
    return new PropertyNamesValidator(parent);
  }

  @Override
  protected String getKeyword() {
    return "propertyNames";
  }

  static class PropertyNamesValidator extends BaseSingleSchemaValidator {

    public PropertyNamesValidator(MutableStateValidator parent) {
      super(parent);
    }

    @Override
    public void validateSync(ValidatorContext context, Object in) throws ValidationException, NoSyncValidationException {
      this.checkSync();
      // attempt to handle JsonObject as Map
      if (in instanceof JsonObject) {
        in = ((JsonObject) in).getMap();
      }
      if (in instanceof Map) {
        ((Map<String, ?>) in).keySet().forEach(k -> schema.validateSync(context.lowerLevelContext(), k));
      }
    }

    @Override
    public Future<Void> validateAsync(ValidatorContext context, Object in) {
      if (isSync()) return validateSyncAsAsync(context, in);
      // attempt to handle JsonObject as Map
      final Object orig = in;
      if (in instanceof JsonObject) {
        in = ((JsonObject) in).getMap();
      }
      if (in instanceof Map) {
        return CompositeFuture.all(
          ((Map<String, ?>) in).keySet()
            .stream()
            .map(k -> schema.validateAsync(context.lowerLevelContext(), k))
            .collect(Collectors.toList())
        ).compose(
          cf -> Future.succeededFuture(),
          err -> Future.failedFuture(ValidationException.createException("provided object contains a key not matching the propertyNames schema", "propertyNames", orig, err))
        );
      } else return Future.succeededFuture();
    }
  }

}
