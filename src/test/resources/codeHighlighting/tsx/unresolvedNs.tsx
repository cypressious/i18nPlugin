export const test1 = (i18n:any) => {
    return (<div>{i18n.t("<warning descr="Unresolved namespace">unresolved</warning>:tst1.base")}</div>);
};