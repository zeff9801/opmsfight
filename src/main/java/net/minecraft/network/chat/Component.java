package net.minecraft.network.chat;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.stream.JsonReader;
import com.mojang.brigadier.Message;
import yesman.epicfight.world.capabilities.item.Style;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;

public interface Component extends Message, FormattedText {
    Style getStyle();

    ComponentContents getContents();

    default String getString() {
        return super.getString();
    }

    default String getString(int p_130669_) {
        StringBuilder $$1 = new StringBuilder();
        this.visit((p_130673_) -> {
            int $$3 = p_130669_ - $$1.length();
            if ($$3 <= 0) {
                return STOP_ITERATION;
            } else {
                $$1.append(p_130673_.length() <= $$3 ? p_130673_ : p_130673_.substring(0, $$3));
                return Optional.empty();
            }
        });
        return $$1.toString();
    }

    List<Component> getSiblings();

    default MutableComponent plainCopy() {
        return MutableComponent.create(this.getContents());
    }

    default MutableComponent copy() {
        return new MutableComponent(this.getContents(), new ArrayList(this.getSiblings()), this.getStyle());
    }

    FormattedCharSequence getVisualOrderText();

    default <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> p_130679_, Style p_130680_) {
        Style $$2 = this.getStyle().applyTo(p_130680_);
        Optional<T> $$3 = this.getContents().visit(p_130679_, $$2);
        if ($$3.isPresent()) {
            return $$3;
        } else {
            Iterator var5 = this.getSiblings().iterator();

            Optional $$5;
            do {
                if (!var5.hasNext()) {
                    return Optional.empty();
                }

                Component $$4 = (Component)var5.next();
                $$5 = $$4.visit(p_130679_, $$2);
            } while(!$$5.isPresent());

            return $$5;
        }
    }

    default <T> Optional<T> visit(FormattedText.ContentConsumer<T> p_130677_) {
        Optional<T> $$1 = this.getContents().visit(p_130677_);
        if ($$1.isPresent()) {
            return $$1;
        } else {
            Iterator var3 = this.getSiblings().iterator();

            Optional $$3;
            do {
                if (!var3.hasNext()) {
                    return Optional.empty();
                }

                Component $$2 = (Component)var3.next();
                $$3 = $$2.visit(p_130677_);
            } while(!$$3.isPresent());

            return $$3;
        }
    }

    default List<Component> toFlatList() {
        return this.toFlatList(Style.EMPTY);
    }

    default List<Component> toFlatList(Style p_178406_) {
        List<Component> $$1 = Lists.newArrayList();
        this.visit((p_178403_, p_178404_) -> {
            if (!p_178404_.isEmpty()) {
                $$1.add(literal(p_178404_).withStyle(p_178403_));
            }

            return Optional.empty();
        }, p_178406_);
        return $$1;
    }

    default boolean contains(Component p_240571_) {
        if (this.equals(p_240571_)) {
            return true;
        } else {
            List<Component> $$1 = this.toFlatList();
            List<Component> $$2 = p_240571_.toFlatList(this.getStyle());
            return Collections.indexOfSubList($$1, $$2) != -1;
        }
    }

    static Component nullToEmpty(@Nullable String p_130675_) {
        return (Component)(p_130675_ != null ? literal(p_130675_) : CommonComponents.EMPTY);
    }

    static MutableComponent literal(String p_237114_) {
        return MutableComponent.create(new LiteralContents(p_237114_));
    }

    static MutableComponent translatable(String p_237116_) {
        return MutableComponent.create(new TranslatableContents(p_237116_, (String)null, TranslatableContents.NO_ARGS));
    }

    static MutableComponent translatable(String p_237111_, Object... p_237112_) {
        return MutableComponent.create(new TranslatableContents(p_237111_, (String)null, p_237112_));
    }

    static MutableComponent translatableWithFallback(String p_265747_, @Nullable String p_265287_) {
        return MutableComponent.create(new TranslatableContents(p_265747_, p_265287_, TranslatableContents.NO_ARGS));
    }

    static MutableComponent translatableWithFallback(String p_265449_, @Nullable String p_265281_, Object... p_265785_) {
        return MutableComponent.create(new TranslatableContents(p_265449_, p_265281_, p_265785_));
    }

    static MutableComponent empty() {
        return MutableComponent.create(ComponentContents.EMPTY);
    }

    static MutableComponent keybind(String p_237118_) {
        return MutableComponent.create(new KeybindContents(p_237118_));
    }

    static MutableComponent nbt(String p_237106_, boolean p_237107_, Optional<Component> p_237108_, DataSource p_237109_) {
        return MutableComponent.create(new NbtContents(p_237106_, p_237107_, p_237108_, p_237109_));
    }

    static MutableComponent score(String p_237100_, String p_237101_) {
        return MutableComponent.create(new ScoreContents(p_237100_, p_237101_));
    }

    static MutableComponent selector(String p_237103_, Optional<Component> p_237104_) {
        return MutableComponent.create(new SelectorContents(p_237103_, p_237104_));
    }

    public static class Serializer implements JsonDeserializer<MutableComponent>, JsonSerializer<Component> {
        private static final Gson GSON = (Gson)Util.make(() -> {
            GsonBuilder $$0 = new GsonBuilder();
            $$0.disableHtmlEscaping();
            $$0.registerTypeHierarchyAdapter(Component.class, new Serializer());
            $$0.registerTypeHierarchyAdapter(Style.class, new Style.Serializer());
            $$0.registerTypeAdapterFactory(new LowerCaseEnumTypeAdapterFactory());
            return $$0.create();
        });
        private static final Field JSON_READER_POS = (Field)Util.make(() -> {
            try {
                new JsonReader(new StringReader(""));
                Field $$0 = JsonReader.class.getDeclaredField("pos");
                $$0.setAccessible(true);
                return $$0;
            } catch (NoSuchFieldException var1) {
                NoSuchFieldException $$1 = var1;
                throw new IllegalStateException("Couldn't get field 'pos' for JsonReader", $$1);
            }
        });
        private static final Field JSON_READER_LINESTART = (Field)Util.make(() -> {
            try {
                new JsonReader(new StringReader(""));
                Field $$0 = JsonReader.class.getDeclaredField("lineStart");
                $$0.setAccessible(true);
                return $$0;
            } catch (NoSuchFieldException var1) {
                NoSuchFieldException $$1 = var1;
                throw new IllegalStateException("Couldn't get field 'lineStart' for JsonReader", $$1);
            }
        });

        public Serializer() {
        }

        public MutableComponent deserialize(JsonElement p_130694_, Type p_130695_, JsonDeserializationContext p_130696_) throws JsonParseException {
            if (p_130694_.isJsonPrimitive()) {
                return Component.literal(p_130694_.getAsString());
            } else {
                MutableComponent $$27;
                if (!p_130694_.isJsonObject()) {
                    if (p_130694_.isJsonArray()) {
                        JsonArray $$30 = p_130694_.getAsJsonArray();
                        $$27 = null;
                        Iterator var17 = $$30.iterator();

                        while(var17.hasNext()) {
                            JsonElement $$32 = (JsonElement)var17.next();
                            MutableComponent $$33 = this.deserialize($$32, $$32.getClass(), p_130696_);
                            if ($$27 == null) {
                                $$27 = $$33;
                            } else {
                                $$27.append($$33);
                            }
                        }

                        return $$27;
                    } else {
                        throw new JsonParseException("Don't know how to turn " + p_130694_ + " into a Component");
                    }
                } else {
                    JsonObject $$3 = p_130694_.getAsJsonObject();
                    String $$19;
                    if ($$3.has("text")) {
                        $$19 = GsonHelper.getAsString($$3, "text");
                        $$27 = $$19.isEmpty() ? Component.empty() : Component.literal($$19);
                    } else if ($$3.has("translate")) {
                        $$19 = GsonHelper.getAsString($$3, "translate");
                        String $$7 = GsonHelper.getAsString($$3, "fallback", (String)null);
                        if ($$3.has("with")) {
                            JsonArray $$8 = GsonHelper.getAsJsonArray($$3, "with");
                            Object[] $$9 = new Object[$$8.size()];

                            for(int $$10 = 0; $$10 < $$9.length; ++$$10) {
                                $$9[$$10] = unwrapTextArgument(this.deserialize($$8.get($$10), p_130695_, p_130696_));
                            }

                            $$27 = Component.translatableWithFallback($$19, $$7, $$9);
                        } else {
                            $$27 = Component.translatableWithFallback($$19, $$7);
                        }
                    } else if ($$3.has("score")) {
                        JsonObject $$13 = GsonHelper.getAsJsonObject($$3, "score");
                        if (!$$13.has("name") || !$$13.has("objective")) {
                            throw new JsonParseException("A score component needs a least a name and an objective");
                        }

                        $$27 = Component.score(GsonHelper.getAsString($$13, "name"), GsonHelper.getAsString($$13, "objective"));
                    } else if ($$3.has("selector")) {
                        Optional<Component> $$16 = this.parseSeparator(p_130695_, p_130696_, $$3);
                        $$27 = Component.selector(GsonHelper.getAsString($$3, "selector"), $$16);
                    } else if ($$3.has("keybind")) {
                        $$27 = Component.keybind(GsonHelper.getAsString($$3, "keybind"));
                    } else {
                        if (!$$3.has("nbt")) {
                            throw new JsonParseException("Don't know how to turn " + p_130694_ + " into a Component");
                        }

                        $$19 = GsonHelper.getAsString($$3, "nbt");
                        Optional<Component> $$20 = this.parseSeparator(p_130695_, p_130696_, $$3);
                        boolean $$21 = GsonHelper.getAsBoolean($$3, "interpret", false);
                        Object $$25;
                        if ($$3.has("block")) {
                            $$25 = new BlockDataSource(GsonHelper.getAsString($$3, "block"));
                        } else if ($$3.has("entity")) {
                            $$25 = new EntityDataSource(GsonHelper.getAsString($$3, "entity"));
                        } else {
                            if (!$$3.has("storage")) {
                                throw new JsonParseException("Don't know how to turn " + p_130694_ + " into a Component");
                            }

                            $$25 = new StorageDataSource(new ResourceLocation(GsonHelper.getAsString($$3, "storage")));
                        }

                        $$27 = Component.nbt($$19, $$21, $$20, (DataSource)$$25);
                    }

                    if ($$3.has("extra")) {
                        JsonArray $$28 = GsonHelper.getAsJsonArray($$3, "extra");
                        if ($$28.size() <= 0) {
                            throw new JsonParseException("Unexpected empty array of components");
                        }

                        for(int $$29 = 0; $$29 < $$28.size(); ++$$29) {
                            $$27.append(this.deserialize($$28.get($$29), p_130695_, p_130696_));
                        }
                    }

                    $$27.setStyle((Style)p_130696_.deserialize(p_130694_, Style.class));
                    return $$27;
                }
            }
        }

        private static Object unwrapTextArgument(Object p_237121_) {
            if (p_237121_ instanceof Component $$1) {
                if ($$1.getStyle().isEmpty() && $$1.getSiblings().isEmpty()) {
                    ComponentContents $$2 = $$1.getContents();
                    if ($$2 instanceof LiteralContents) {
                        LiteralContents $$3 = (LiteralContents)$$2;
                        return $$3.text();
                    }
                }
            }

            return p_237121_;
        }

        private Optional<Component> parseSeparator(Type p_178416_, JsonDeserializationContext p_178417_, JsonObject p_178418_) {
            return p_178418_.has("separator") ? Optional.of(this.deserialize(p_178418_.get("separator"), p_178416_, p_178417_)) : Optional.empty();
        }

        private void serializeStyle(Style p_130710_, JsonObject p_130711_, JsonSerializationContext p_130712_) {
            JsonElement $$3 = p_130712_.serialize(p_130710_);
            if ($$3.isJsonObject()) {
                JsonObject $$4 = (JsonObject)$$3;
                Iterator var6 = $$4.entrySet().iterator();

                while(var6.hasNext()) {
                    Map.Entry<String, JsonElement> $$5 = (Map.Entry)var6.next();
                    p_130711_.add((String)$$5.getKey(), (JsonElement)$$5.getValue());
                }
            }

        }

        public JsonElement serialize(Component p_130706_, Type p_130707_, JsonSerializationContext p_130708_) {
            JsonObject $$3 = new JsonObject();
            if (!p_130706_.getStyle().isEmpty()) {
                this.serializeStyle(p_130706_.getStyle(), $$3, p_130708_);
            }

            if (!p_130706_.getSiblings().isEmpty()) {
                JsonArray $$4 = new JsonArray();
                Iterator var6 = p_130706_.getSiblings().iterator();

                while(var6.hasNext()) {
                    Component $$5 = (Component)var6.next();
                    $$4.add(this.serialize((Component)$$5, Component.class, p_130708_));
                }

                $$3.add("extra", $$4);
            }

            ComponentContents $$6 = p_130706_.getContents();
            if ($$6 == ComponentContents.EMPTY) {
                $$3.addProperty("text", "");
            } else if ($$6 instanceof LiteralContents) {
                LiteralContents $$7 = (LiteralContents)$$6;
                $$3.addProperty("text", $$7.text());
            } else if ($$6 instanceof TranslatableContents) {
                TranslatableContents $$8 = (TranslatableContents)$$6;
                $$3.addProperty("translate", $$8.getKey());
                String $$9 = $$8.getFallback();
                if ($$9 != null) {
                    $$3.addProperty("fallback", $$9);
                }

                if ($$8.getArgs().length > 0) {
                    JsonArray $$10 = new JsonArray();
                    Object[] var14 = $$8.getArgs();
                    int var15 = var14.length;

                    for(int var16 = 0; var16 < var15; ++var16) {
                        Object $$11 = var14[var16];
                        if ($$11 instanceof Component) {
                            $$10.add(this.serialize((Component)((Component)$$11), $$11.getClass(), p_130708_));
                        } else {
                            $$10.add(new JsonPrimitive(String.valueOf($$11)));
                        }
                    }

                    $$3.add("with", $$10);
                }
            } else if ($$6 instanceof ScoreContents) {
                ScoreContents $$12 = (ScoreContents)$$6;
                JsonObject $$13 = new JsonObject();
                $$13.addProperty("name", $$12.getName());
                $$13.addProperty("objective", $$12.getObjective());
                $$3.add("score", $$13);
            } else if ($$6 instanceof SelectorContents) {
                SelectorContents $$14 = (SelectorContents)$$6;
                $$3.addProperty("selector", $$14.getPattern());
                this.serializeSeparator(p_130708_, $$3, $$14.getSeparator());
            } else if ($$6 instanceof KeybindContents) {
                KeybindContents $$15 = (KeybindContents)$$6;
                $$3.addProperty("keybind", $$15.getName());
            } else {
                if (!($$6 instanceof NbtContents)) {
                    throw new IllegalArgumentException("Don't know how to serialize " + $$6 + " as a Component");
                }

                NbtContents $$16 = (NbtContents)$$6;
                $$3.addProperty("nbt", $$16.getNbtPath());
                $$3.addProperty("interpret", $$16.isInterpreting());
                this.serializeSeparator(p_130708_, $$3, $$16.getSeparator());
                DataSource $$17 = $$16.getDataSource();
                if ($$17 instanceof BlockDataSource) {
                    BlockDataSource $$18 = (BlockDataSource)$$17;
                    $$3.addProperty("block", $$18.posPattern());
                } else if ($$17 instanceof EntityDataSource) {
                    EntityDataSource $$19 = (EntityDataSource)$$17;
                    $$3.addProperty("entity", $$19.selectorPattern());
                } else {
                    if (!($$17 instanceof StorageDataSource)) {
                        throw new IllegalArgumentException("Don't know how to serialize " + $$6 + " as a Component");
                    }

                    StorageDataSource $$20 = (StorageDataSource)$$17;
                    $$3.addProperty("storage", $$20.id().toString());
                }
            }

            return $$3;
        }

        private void serializeSeparator(JsonSerializationContext p_178412_, JsonObject p_178413_, Optional<Component> p_178414_) {
            p_178414_.ifPresent((p_178410_) -> {
                p_178413_.add("separator", this.serialize((Component)p_178410_, p_178410_.getClass(), p_178412_));
            });
        }

        public static String toJson(Component p_130704_) {
            return GSON.toJson(p_130704_);
        }

        public static String toStableJson(Component p_237123_) {
            return GsonHelper.toStableString(toJsonTree(p_237123_));
        }

        public static JsonElement toJsonTree(Component p_130717_) {
            return GSON.toJsonTree(p_130717_);
        }

        @Nullable
        public static MutableComponent fromJson(String p_130702_) {
            return (MutableComponent)GsonHelper.fromNullableJson(GSON, p_130702_, MutableComponent.class, false);
        }

        @Nullable
        public static MutableComponent fromJson(JsonElement p_130692_) {
            return (MutableComponent)GSON.fromJson(p_130692_, MutableComponent.class);
        }

        @Nullable
        public static MutableComponent fromJsonLenient(String p_130715_) {
            return (MutableComponent)GsonHelper.fromNullableJson(GSON, p_130715_, MutableComponent.class, true);
        }

        public static MutableComponent fromJson(com.mojang.brigadier.StringReader p_130700_) {
            try {
                JsonReader $$1 = new JsonReader(new StringReader(p_130700_.getRemaining()));
                $$1.setLenient(false);
                MutableComponent $$2 = (MutableComponent)GSON.getAdapter(MutableComponent.class).read($$1);
                p_130700_.setCursor(p_130700_.getCursor() + getPos($$1));
                return $$2;
            } catch (StackOverflowError | IOException var3) {
                Throwable $$3 = var3;
                throw new JsonParseException($$3);
            }
        }

        private static int getPos(JsonReader p_130698_) {
            try {
                return JSON_READER_POS.getInt(p_130698_) - JSON_READER_LINESTART.getInt(p_130698_) + 1;
            } catch (IllegalAccessException var2) {
                IllegalAccessException $$1 = var2;
                throw new IllegalStateException("Couldn't read position of JsonReader", $$1);
            }
        }
    }
}
