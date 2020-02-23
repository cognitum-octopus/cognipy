namespace Ontorion
{
    public struct GraphEntity
    {
        public string Name { get; set; }
        public string Kind { get; set; }
        public override string ToString()
        {
            return Name;
        }
    }
}
