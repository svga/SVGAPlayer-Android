// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: common.proto
package common;

import com.squareup.wire.ProtoAdapter;
import com.squareup.wire.WireEnum;
import java.lang.Override;

public enum ESex implements WireEnum {
  kSexFemale(0),

  kSexMale(1),

  /**
   * 用于性别过滤场景
   */
  kSexAll(2);

  public static final ProtoAdapter<ESex> ADAPTER = ProtoAdapter.newEnumAdapter(ESex.class);

  private final int value;

  ESex(int value) {
    this.value = value;
  }

  /**
   * Return the constant for {@code value} or null.
   */
  public static ESex fromValue(int value) {
    switch (value) {
      case 0: return kSexFemale;
      case 1: return kSexMale;
      case 2: return kSexAll;
      default: return null;
    }
  }

  @Override
  public int getValue() {
    return value;
  }
}