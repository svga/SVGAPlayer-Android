// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: cproxy.proto
package ikxd.cproxy;

import android.os.Parcelable;
import com.squareup.wire.AndroidMessage;
import com.squareup.wire.Message;
import com.squareup.wire.ProtoAdapter;
import com.squareup.wire.WireField;
import com.squareup.wire.internal.Internal;
import common.Header;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import okio.ByteString;

/**
 * cproxy内部使用
 */
public final class Inner extends AndroidMessage<Inner, Inner.Builder> {
  public static final ProtoAdapter<Inner> ADAPTER = ProtoAdapter.newMessageAdapter(Inner.class);

  public static final Creator<Inner> CREATOR = AndroidMessage.newCreator(ADAPTER);

  private static final long serialVersionUID = 0L;

  public static final Integer DEFAULT_URI = 0;

  @WireField(
      tag = 1,
      adapter = "common.Header#ADAPTER"
  )
  public final Header header;

  @WireField(
      tag = 2,
      adapter = "com.squareup.wire.ProtoAdapter#INT32"
  )
  public final Integer uri;

  public Inner(Header header, Integer uri) {
    this(header, uri, ByteString.EMPTY);
  }

  public Inner(Header header, Integer uri, ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.header = header;
    this.uri = uri;
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.header = header;
    builder.uri = uri;
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof Inner)) return false;
    Inner o = (Inner) other;
    return unknownFields().equals(o.unknownFields())
        && Internal.equals(header, o.header)
        && Internal.equals(uri, o.uri);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + (header != null ? header.hashCode() : 0);
      result = result * 37 + (uri != null ? uri.hashCode() : 0);
      super.hashCode = result;
    }
    return result;
  }

  public static final class Builder extends Message.Builder<Inner, Builder> {
    public Header header;

    public Integer uri;

    public Builder() {
    }

    public Builder header(Header header) {
      this.header = header;
      return this;
    }

    public Builder uri(Integer uri) {
      this.uri = uri;
      return this;
    }

    @Override
    public Inner build() {
      return new Inner(header, uri, super.buildUnknownFields());
    }
  }
}