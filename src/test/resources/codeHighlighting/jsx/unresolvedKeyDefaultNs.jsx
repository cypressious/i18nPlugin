export const test1 = (i18n) => {
    return (<div>{i18n.t("tst1.<warning descr="Unresolved key">unresolved.part.of.key.jsx</warning>")}</div>);
};