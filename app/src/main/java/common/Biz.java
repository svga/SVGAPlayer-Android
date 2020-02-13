// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: common.proto
package common;

import android.os.Parcelable;
import com.squareup.wire.AndroidMessage;
import com.squareup.wire.Message;
import com.squareup.wire.ProtoAdapter;
import com.squareup.wire.WireField;
import com.squareup.wire.internal.Internal;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.List;
import java.util.Map;
import okio.ByteString;

/**
 * BIZ 对应的是Header.biz字段，用于给业务做一些透传信息
 * 使用方法：
 *     服务器通过biz_actions告诉客户端更新biz信息，客户端将信息保存在内存里面
 *     客户端给服务端发包的时候，需要将对应的sname的biz信息填充在biz_infos字段里面
 */
public final class Biz extends AndroidMessage<Biz, Biz.Builder> {
  public static final ProtoAdapter<Biz> ADAPTER = ProtoAdapter.newMessageAdapter(Biz.class);

  public static final Creator<Biz> CREATOR = AndroidMessage.newCreator(ADAPTER);

  private static final long serialVersionUID = 0L;

  /**
   * see EBizType
   */
  @WireField(
      tag = 1,
      adapter = "com.squareup.wire.ProtoAdapter#INT64",
      label = WireField.Label.REPEATED
  )
  public final List<Long> types;

  /**
   * 各列的编号和EBizType保持一致
   * 下行包携带，用于给服务端控制客户端的biz信息
   */
  @WireField(
      tag = 10,
      adapter = "common.BizAction#ADAPTER",
      label = WireField.Label.REPEATED
  )
  public final List<BizAction> biz_actions;

  /**
   * 上行包携带，客户端携带这些信息给对应的sname
   */
  @WireField(
      tag = 11,
      keyAdapter = "com.squareup.wire.ProtoAdapter#STRING",
      adapter = "com.squareup.wire.ProtoAdapter#BYTES"
  )
  public final Map<String, ByteString> biz_infos;

  /**
   * 一些扩展的字段，上下行都有可能，由业务自行决定怎么用
   */
  @WireField(
      tag = 12,
      keyAdapter = "com.squareup.wire.ProtoAdapter#STRING",
      adapter = "com.squareup.wire.ProtoAdapter#BYTES"
  )
  public final Map<String, ByteString> biz_ext;

  public Biz(List<Long> types, List<BizAction> biz_actions, Map<String, ByteString> biz_infos,
      Map<String, ByteString> biz_ext) {
    this(types, biz_actions, biz_infos, biz_ext, ByteString.EMPTY);
  }

  public Biz(List<Long> types, List<BizAction> biz_actions, Map<String, ByteString> biz_infos,
      Map<String, ByteString> biz_ext, ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.types = Internal.immutableCopyOf("types", types);
    this.biz_actions = Internal.immutableCopyOf("biz_actions", biz_actions);
    this.biz_infos = Internal.immutableCopyOf("biz_infos", biz_infos);
    this.biz_ext = Internal.immutableCopyOf("biz_ext", biz_ext);
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.types = Internal.copyOf(types);
    builder.biz_actions = Internal.copyOf(biz_actions);
    builder.biz_infos = Internal.copyOf(biz_infos);
    builder.biz_ext = Internal.copyOf(biz_ext);
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof Biz)) return false;
    Biz o = (Biz) other;
    return unknownFields().equals(o.unknownFields())
        && types.equals(o.types)
        && biz_actions.equals(o.biz_actions)
        && biz_infos.equals(o.biz_infos)
        && biz_ext.equals(o.biz_ext);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + types.hashCode();
      result = result * 37 + biz_actions.hashCode();
      result = result * 37 + biz_infos.hashCode();
      result = result * 37 + biz_ext.hashCode();
      super.hashCode = result;
    }
    return result;
  }

  public static final class Builder extends Message.Builder<Biz, Builder> {
    public List<Long> types;

    public List<BizAction> biz_actions;

    public Map<String, ByteString> biz_infos;

    public Map<String, ByteString> biz_ext;

    public Builder() {
      types = Internal.newMutableList();
      biz_actions = Internal.newMutableList();
      biz_infos = Internal.newMutableMap();
      biz_ext = Internal.newMutableMap();
    }

    /**
     * see EBizType
     */
    public Builder types(List<Long> types) {
      Internal.checkElementsNotNull(types);
      this.types = types;
      return this;
    }

    /**
     * 各列的编号和EBizType保持一致
     * 下行包携带，用于给服务端控制客户端的biz信息
     */
    public Builder biz_actions(List<BizAction> biz_actions) {
      Internal.checkElementsNotNull(biz_actions);
      this.biz_actions = biz_actions;
      return this;
    }

    /**
     * 上行包携带，客户端携带这些信息给对应的sname
     */
    public Builder biz_infos(Map<String, ByteString> biz_infos) {
      Internal.checkElementsNotNull(biz_infos);
      this.biz_infos = biz_infos;
      return this;
    }

    /**
     * 一些扩展的字段，上下行都有可能，由业务自行决定怎么用
     */
    public Builder biz_ext(Map<String, ByteString> biz_ext) {
      Internal.checkElementsNotNull(biz_ext);
      this.biz_ext = biz_ext;
      return this;
    }

    @Override
    public Biz build() {
      return new Biz(types, biz_actions, biz_infos, biz_ext, super.buildUnknownFields());
    }
  }
}